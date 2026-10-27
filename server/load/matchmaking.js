// k6 load test: players arrive at a steady rate, join the queue, poll until matched, and one
// player of each pair reports the result. Measures what a player experiences (time to match)
// as well as raw API latency, and fails the run if the SLOs below are missed.
//
//   k6 run load/matchmaking.js                                   # 100 players/s for 2 min
//   k6 run -e RATE=300 -e DURATION=5m load/matchmaking.js
//   k6 run -e BASE_URL=http://matchmaker:8080 -e RATE=20 -e DURATION=30s load/matchmaking.js   # CI smoke

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';
import exec from 'k6/execution';

const BASE = __ENV.BASE_URL || 'http://localhost:8080';
const RATE = Number(__ENV.RATE || 100);          // players joining per second
const DURATION = __ENV.DURATION || '2m';         // time at full rate
const POLL_S = Number(__ENV.POLL_S || 0.25);
const GAMES = ['tictactoe', 'checkers', 'connect4'];
const JSON_HEADERS = { headers: { 'Content-Type': 'application/json' } };

const timeToMatch = new Trend('time_to_match', true);
const matched = new Rate('matched');
const results = new Counter('results_reported');

export const options = {
  scenarios: {
    players: {
      executor: 'ramping-arrival-rate',
      startRate: 0,
      timeUnit: '1s',
      preAllocatedVUs: Math.max(50, RATE * 3),
      maxVUs: Math.max(200, RATE * 12),
      stages: [
        { target: RATE, duration: '20s' },   // ramp up
        { target: RATE, duration: DURATION },
        { target: 0, duration: '10s' },
      ],
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    'http_req_duration{name:join}': ['p(95)<100', 'p(99)<250'],
    'http_req_duration{name:poll}': ['p(95)<50', 'p(99)<150'],
    'http_req_duration{name:result}': ['p(95)<150'],
    time_to_match: ['p(95)<10000'],   // the SLO: 95 % of players matched within 10 s
    matched: ['rate>0.99'],
  },
  summaryTrendStats: ['avg', 'p(50)', 'p(95)', 'p(99)', 'max'],
};

// Player skill: roughly normal around 1500.
function rating() {
  const u = 1 - Math.random();
  const v = Math.random();
  return Math.round(1500 + 250 * Math.sqrt(-2 * Math.log(u)) * Math.cos(2 * Math.PI * v));
}

export default function () {
  const n = exec.scenario.iterationInTest;
  const me = `k6-${exec.scenario.startTime}-${n}`;
  const game = GAMES[n % GAMES.length];

  const join = http.post(`${BASE}/v1/tickets`,
    JSON.stringify({ playerId: me, game: game, ratingHint: rating() }),
    Object.assign({ tags: { name: 'join' } }, JSON_HEADERS));
  if (!check(join, { 'joined (201)': (r) => r.status === 201 })) {
    matched.add(false);
    return;
  }
  const ticket = join.json('ticketId');

  for (let i = 0; i < 300; i++) {
    sleep(POLL_S);
    const poll = http.get(`${BASE}/v1/tickets/${ticket}`, { tags: { name: 'poll' } });
    if (!check(poll, { 'polled (200)': (r) => r.status === 200 })) {
      matched.add(false);
      return;
    }
    const status = poll.json('status');
    if (status === 'SEARCHING') {
      continue;
    }
    matched.add(status === 'MATCHED');
    if (status !== 'MATCHED') {
      return;
    }
    timeToMatch.add(poll.json('waitedMillis'));
    const opponent = poll.json('opponentId');
    if (me < opponent) {  // exactly one of the pair reports
      const roll = Math.random();
      const winner = roll < 0.1 ? null : roll < 0.55 ? me : opponent;
      const res = http.post(`${BASE}/v1/matches/${poll.json('matchId')}/result`,
        JSON.stringify({ reportedBy: me, winner: winner }),
        Object.assign({ tags: { name: 'result' } }, JSON_HEADERS));
      check(res, { 'result recorded (200)': (r) => r.status === 200 });
      results.add(1);
    }
    return;
  }
  matched.add(false);
}
