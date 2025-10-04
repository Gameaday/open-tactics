# OWASP Dependency Check Timeout Fix

## Problem
The OWASP dependency check was causing build failures in CI/CD pipelines due to:
- Slow NVD (National Vulnerability Database) data downloads without an API key
- Taking 30+ minutes to update, causing timeouts and cancellations
- Error message: "An NVD API Key was not provided - it is highly recommended to use an NVD API key as the update can take a VERY long time without an API Key"

## Root Cause
The OWASP dependency check plugin was configured to automatically update the NVD database on every run. Without an API key, this process:
- Makes API calls with a default 8-second delay between calls
- Downloads large amounts of CVE data incrementally
- Can take 30+ minutes on fresh installations or after cache expiration

## Solution Implemented

### 1. NVD Data Caching Configuration
**File:** `build.gradle.kts`

```kotlin
nvd.apply {
    // Use cached data for 7 days to minimize slow NVD updates
    // Without an API key, NVD updates can take 30+ minutes
    validForHours = 168 // 7 days
    
    // Increase delay between API calls to avoid rate limiting
    delay = 8000
}
```

**Key Changes:**
- `validForHours = 168`: Cache NVD data for 7 days instead of updating every run
- `delay = 8000`: Maintain proper delay to avoid rate limiting when updates do occur

### 2. CI/CD Workflow Optimization
**File:** `.github/workflows/qa.yml`

**Cache Step Added:**
```yaml
- name: Cache OWASP NVD data
  uses: actions/cache@v4
  with:
    path: |
      ~/.gradle/dependency-check-data
    key: ${{ runner.os }}-owasp-nvd-${{ hashFiles('**/build.gradle.kts') }}
    restore-keys: |
      ${{ runner.os }}-owasp-nvd-
```

**Timeout and Error Handling:**
```yaml
- name: Run OWASP dependency check
  run: ./gradlew dependencyCheckAnalyze
  continue-on-error: true
  timeout-minutes: 25
```

**Job-Level Timeout:**
```yaml
security-analysis:
  timeout-minutes: 30
```

## Benefits

### For Local Development
- **First run**: 20-30 minutes to download NVD data
- **Subsequent runs (within 7 days)**: Seconds to complete
- No build failures due to timeouts

### For CI/CD Pipeline
- **GitHub Actions cache**: Persists NVD data between workflow runs
- **No pipeline blocking**: `continue-on-error: true` prevents blocking other jobs
- **Adequate time**: 30-minute job timeout allows first run to complete
- **Fast subsequent runs**: Cached data ensures quick checks

## Expected Behavior

### First Run (Fresh Cache)
```
> Task :dependencyCheckUpdate
Downloading NVD data...
[This can take 20-30 minutes]

> Task :dependencyCheckAnalyze
Checking dependencies...
BUILD SUCCESSFUL in 25m
```

### Subsequent Runs (Within 7 Days)
```
> Task :dependencyCheckAnalyze
Using cached NVD data (valid for 6 days)
Checking dependencies...
BUILD SUCCESSFUL in 8s
```

### After 7 Days
```
> Task :dependencyCheckUpdate
NVD data expired, updating...
[Takes 20-30 minutes again]
```

## Verification

### Local Testing
```bash
# First run (may take 20-30 minutes)
./gradlew dependencyCheckAnalyze

# Second run (should be fast)
./gradlew dependencyCheckAnalyze

# Force update by deleting cache
rm -rf ~/.gradle/dependency-check-data
./gradlew dependencyCheckAnalyze
```

### CI/CD Testing
- Push changes to a branch
- Check GitHub Actions workflow for `QA - Quality Assurance`
- Verify the `Security Analysis` job completes successfully
- Check that subsequent runs use cached data

## Optional: Adding an NVD API Key

To speed up the first run, you can obtain a free NVD API key:

1. Request a key at: https://nvd.nist.gov/developers/request-an-api-key
2. Add to `build.gradle.kts`:
```kotlin
nvd.apply {
    apiKey = System.getenv("NVD_API_KEY") ?: ""
    validForHours = 168
    delay = 3500 // Faster with API key
}
```
3. Set as GitHub secret: `NVD_API_KEY`
4. Add to workflow:
```yaml
- name: Run OWASP dependency check
  run: ./gradlew dependencyCheckAnalyze
  env:
    NVD_API_KEY: ${{ secrets.NVD_API_KEY }}
```

With an API key, updates take ~5 minutes instead of 30+ minutes.

## Related Documentation
- [BUILD_ISSUES_RESOLUTION.md](BUILD_ISSUES_RESOLUTION.md) - Section 3: OWASP Optimization
- [BUILD_VERIFICATION_GUIDE.md](BUILD_VERIFICATION_GUIDE.md) - Security Analysis section
- [OWASP Dependency Check Documentation](https://jeremylong.github.io/DependencyCheck/dependency-check-gradle/configuration.html)
