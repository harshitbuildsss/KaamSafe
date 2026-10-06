export default function ScoreCard({ score, condition }) {
  return (
    <div className="score-card">
      <span>Workability</span>
      <strong>{score ?? '--'}<small>/100</small></strong>
      <em>{condition ?? 'Waiting for analysis'}</em>
    </div>
  );
}
