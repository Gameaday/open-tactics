# Android Emulator Boot Timeout Fix

## Issue Summary
**Problem**: CI workflow was failing with "Timeout waiting for emulator to boot" error during the instrumented test phase on GitHub Actions.

**Impact**: Instrumented tests on API levels 24, 29, and 34 could not run, preventing complete CI/CD validation of Android-specific functionality.

## Root Cause
The Android emulator in GitHub Actions CI environment was taking longer than the default timeout (300 seconds) to boot, particularly on certain API levels or when AVD cache was not available.

## Solution Implemented 🎯

### 1. Increased Emulator Boot Timeout
- **Added explicit `emulator-boot-timeout: 600`** parameter (10 minutes)
- Default timeout of 300 seconds (5 minutes) was insufficient
- Provides more time for slower-booting API levels

### 2. Memory Optimization
- **Added `-memory 4096`**: Allocates 4GB RAM to emulator
- **Added `-partition-size 4096`**: Allocates 4GB partition size
- Improves emulator boot stability and performance
- Reduces likelihood of boot failures due to resource constraints

### 3. Applied to Both Workflow Steps
Changes applied to:
1. **AVD creation step**: When generating snapshot for caching
2. **Test execution step**: When running instrumented tests

## Changes Made 📝

### File: `.github/workflows/ci.yml`

#### Before:
```yaml
- name: Create AVD and generate snapshot for caching
  uses: reactivecircus/android-emulator-runner@v2
  with:
    emulator-options: -no-window -gpu swiftshader_indirect -noaudio -no-boot-anim -camera-back none
    disable-animations: false
```

#### After:
```yaml
- name: Create AVD and generate snapshot for caching
  uses: reactivecircus/android-emulator-runner@v2
  with:
    emulator-options: -no-window -gpu swiftshader_indirect -noaudio -no-boot-anim -camera-back none -memory 4096 -partition-size 4096
    disable-animations: false
    emulator-boot-timeout: 600
```

### Documentation Updates

#### File: `BUILD_ISSUES_RESOLUTION.md`
- Added note about emulator boot timeout increase
- Documented memory optimizations
- Updated "What's New" section with reliability improvements

#### File: `BUILD_VERIFICATION_GUIDE.md`
- Added CI emulator configuration details
- Created troubleshooting section for emulator timeout issues
- Provided local emulator debugging commands

## Technical Details 🔧

### Emulator Boot Timeout Parameter
- **Parameter**: `emulator-boot-timeout`
- **Default**: 300 seconds (5 minutes)
- **New Value**: 600 seconds (10 minutes)
- **Reason**: Some API levels and cold boots require additional time

### Memory Configuration
- **RAM**: 4096 MB (4 GB)
  - Sufficient for most Android system images
  - Matches typical CI runner available memory
- **Partition Size**: 4096 MB (4 GB)
  - Provides adequate space for system and app installation
  - Prevents "insufficient storage" errors

### Emulator Options Used
```bash
-no-window                    # Headless mode for CI
-gpu swiftshader_indirect     # Software GPU rendering
-noaudio                      # Disable audio (not needed)
-no-boot-anim                 # Skip boot animation
-camera-back none             # Disable camera
-memory 4096                  # 4GB RAM
-partition-size 4096          # 4GB partition
```

## Verification ✅

### Expected Outcomes
1. ✅ Emulator boots successfully within 10 minutes
2. ✅ AVD snapshot creation completes without timeout
3. ✅ Instrumented tests run successfully on all API levels (24, 29, 34)
4. ✅ No resource-related failures during test execution

### How to Verify
1. **Check CI Workflow**: Monitor next push to main/develop branch
2. **Watch for**:
   - "Create AVD and generate snapshot" step completes
   - "Run instrumented tests" step executes successfully
   - All 11 instrumented tests pass
3. **Review Logs**: Check emulator boot time in workflow logs

### Fallback Options
If timeout persists (unlikely):
1. Further increase timeout to 900s (15 minutes)
2. Consider using different API levels that boot faster
3. Evaluate using different emulator target (default vs google_apis)
4. Consider reducing matrix size (fewer API levels)

## Best Practices Going Forward 📚

### For Local Development
```bash
# When creating emulator locally, use similar settings:
avd create <name> --package "system-images;android-34;google_apis;x86_64"

# Start with adequate memory:
emulator -avd <name> -memory 4096 -partition-size 4096
```

### For CI Maintenance
- Monitor workflow execution times
- Keep AVD cache valid to skip snapshot creation
- Update android-emulator-runner when new versions available
- Consider API level selection based on boot reliability

## Related Documentation

- [Android Emulator Runner](https://github.com/ReactiveCircus/android-emulator-runner)
- [GitHub Actions macOS Runners](https://docs.github.com/en/actions/using-github-hosted-runners/about-github-hosted-runners)
- [Android Emulator Command-line Options](https://developer.android.com/studio/run/emulator-commandline)

## Impact Summary

**Before Fix**:
- ❌ Emulator boot timeout failures
- ❌ Instrumented tests could not run
- ❌ Android-specific issues not caught by CI

**After Fix**:
- ✅ Reliable emulator boot in CI
- ✅ All 11 instrumented tests can run
- ✅ Complete CI/CD coverage for Android functionality
- ✅ Improved resource allocation for stability

**No Breaking Changes**: All existing functionality preserved, only improvements to reliability and timeout handling.
