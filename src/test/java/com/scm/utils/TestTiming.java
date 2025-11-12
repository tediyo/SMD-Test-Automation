package com.scm.utils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class to track test execution timing and response times
 */
public class TestTiming {
    private static final Map<String, Long> stepStartTimes = new ConcurrentHashMap<>();
    private static final Map<String, Long> stepDurations = new ConcurrentHashMap<>();
    private static final Map<String, LocalDateTime> stepTimestamps = new ConcurrentHashMap<>();
    private static final Map<String, String> stepNames = new ConcurrentHashMap<>();
    
    /**
     * Start timing for a test step
     * @param stepId Unique identifier for the step
     * @param stepName Human-readable name of the step
     */
    public static void startStep(String stepId, String stepName) {
        long startTime = System.currentTimeMillis();
        stepStartTimes.put(stepId, startTime);
        stepNames.put(stepId, stepName);
        stepTimestamps.put(stepId, LocalDateTime.now());
    }
    
    /**
     * End timing for a test step
     * @param stepId Unique identifier for the step
     * @return Duration in milliseconds
     */
    public static long endStep(String stepId) {
        Long startTime = stepStartTimes.get(stepId);
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            stepDurations.put(stepId, duration);
            return duration;
        }
        return 0;
    }
    
    /**
     * Get duration for a step in milliseconds
     * @param stepId Unique identifier for the step
     * @return Duration in milliseconds, or 0 if not found
     */
    public static long getStepDuration(String stepId) {
        return stepDurations.getOrDefault(stepId, 0L);
    }
    
    /**
     * Get step name
     * @param stepId Unique identifier for the step
     * @return Step name, or null if not found
     */
    public static String getStepName(String stepId) {
        return stepNames.get(stepId);
    }
    
    /**
     * Get step timestamp
     * @param stepId Unique identifier for the step
     * @return Timestamp, or null if not found
     */
    public static LocalDateTime getStepTimestamp(String stepId) {
        return stepTimestamps.get(stepId);
    }
    
    /**
     * Get all step durations
     * @return Map of step IDs to durations
     */
    public static Map<String, Long> getAllStepDurations() {
        return new ConcurrentHashMap<>(stepDurations);
    }
    
    /**
     * Get all step names
     * @return Map of step IDs to names
     */
    public static Map<String, String> getAllStepNames() {
        return new ConcurrentHashMap<>(stepNames);
    }
    
    /**
     * Clear all timing data
     */
    public static void clear() {
        stepStartTimes.clear();
        stepDurations.clear();
        stepTimestamps.clear();
        stepNames.clear();
    }
    
    /**
     * Format duration in a human-readable format
     * @param milliseconds Duration in milliseconds
     * @return Formatted string (e.g., "1.5s", "250ms")
     */
    public static String formatDuration(long milliseconds) {
        if (milliseconds < 1000) {
            return milliseconds + "ms";
        } else {
            double seconds = milliseconds / 1000.0;
            return String.format("%.2fs", seconds);
        }
    }
}

