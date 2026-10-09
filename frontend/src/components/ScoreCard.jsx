const HEADLINES = {
  GOOD: 'Good conditions for work',
  MODERATE: 'Work with some care',
  DIFFICULT: 'Difficult conditions',
  SEVERE: 'Severe conditions, avoid if you can'
};
const SOURCE = { live: 'Live data', cached: 'Cached data', demo: 'Demo data' };

export default function ScoreCard({ result }) {
  const { workabilityScore, condition, recommendedWorkWindow, environment, factors, dataSource } = result;
  return (
    <div className={`card score-card cond-${condition}`}>
      <span className={`badge source-${dataSource}`}>{SOURCE[dataSource] ?? dataSource}</span>
      <div className="score-main">
        <strong>{workabilityScore}<small>/100</small></strong>
        <div>
          <h2>{HEADLINES[condition] ?? condition}</h2>
          {recommendedWorkWindow && (
            <p>Best time to work: <b>{recommendedWorkWindow.start} to {recommendedWorkWindow.end}</b></p>
          )}
        </div>
      </div>
      <dl className="stats">
        <div><dt>Temperature</dt><dd>{environment.temperatureCelsius}°C</dd></div>
        <div><dt>Air quality (AQI)</dt><dd>{environment.aqi}</dd></div>
        <div><dt>Chance of rain</dt><dd>{environment.rainRiskPercent}%</dd></div>
        <div><dt>Heat risk</dt><dd>{factors.heatRisk}</dd></div>
        <div><dt>Air risk</dt><dd>{factors.aqiRisk}</dd></div>
        <div><dt>Rain risk</dt><dd>{factors.rainRisk}</dd></div>
      </dl>
    </div>
  );
}
