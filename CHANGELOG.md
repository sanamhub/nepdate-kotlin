# Changelog

All notable changes to `io.github.sanamhub:nepdate` and `io.github.sanamhub:nepdate-patro`. Both
artifacts share one version. Format: [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
versions follow [Semantic Versioning](https://semver.org/).

## [Unreleased]

### Added
- `NepaliDate` for BS 1901-01-01 to 2199-12-30: `of`, `ofOrNull`, `ofGregorian`, `ofEpochDay`,
  `MIN`, `MAX`, the BS and AD fields, `dayOfWeek`, `dayOfYear`, `lengthOfMonth()`,
  `firstDayOfMonth()`, `lastDayOfMonth()`, `toEpochDay()`, ordering and `toString()` as `YYYY/MM/DD`.
- `Month`, `Weekday`, `Lang` and `NepDateException` with its `kind`.
