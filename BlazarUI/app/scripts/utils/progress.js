import {filter} from 'underscore';
import {std, mean} from 'mathjs';

function _filterOutliers(durations) {
  const values = durations.concat();

  if (durations.length < 4) {
    return durations;
  }

  values.sort((a, b) => {
    return a - b;
  });

  const q1 = values[Math.floor((values.length / 4))];
  const q3 = values[Math.ceil((values.length * (3 / 4)))];
  const iqr = q3 - q1;
  const maxValue = q3 + iqr * 1.5;
  const minValue = q1 - iqr * 1.5;
  const filteredValues = values.filter((x) => {
    return (x < maxValue) && (x > minValue);
  });

  return filteredValues;
}

function _durations(history) {
  let durations = history.map((build) => {
    let duration;
    if (build.build.startTimestamp !== undefined && build.build.endTimestamp !== undefined) {
      duration = build.build.endTimestamp - build.build.startTimestamp;
    }
    return duration || undefined;
  });
  // Remove invalid values
  durations = filter(durations, (d) => {
    return d !== undefined && d > 0;
  });

  return durations;
}

function progress(startTimestamp, history) {
  // get historical durations of builds
  const durations = _durations(history);
  // remove outliers
  const durationsNoOutliers = _filterOutliers(durations);
  // get standard deviation
  const stdDev = std(durationsNoOutliers);
  // calculate avergae build time
  const avgTime = mean(durationsNoOutliers);
  // pad the average
  const paddedAvgTime = avgTime + (2 * stdDev);
  // time elapsed from now
  const elapsedTime = new Date().getTime() - startTimestamp;
  // calculate progress
  return Math.round((elapsedTime / paddedAvgTime) * 100);
}

export default progress;
