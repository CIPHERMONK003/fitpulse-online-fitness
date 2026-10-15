package com.fitness;

import com.fitness.model.DifficultyLevel;
import com.fitness.model.PlanType;
import com.fitness.model.ProgressRecord;
import com.fitness.model.WorkoutPlan;
import com.fitness.model.StrengthWorkoutPlan;
import com.fitness.util.ApiResponse;
import com.fitness.util.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Unit Tests for Collections & Generics (6 Marks):
 * - Generic classes & methods (ApiResponse<T>, CollectionUtils)
 * - Java Collections Framework (List, Map, Set)
 * - Custom Comparators
 * - Java Streams API (filter, map, groupingBy, summarizing)
 */
public class CollectionsAndGenericsTest {

    @Test
    public void testGenericsApiResponse() {
        // Generic response with String
        ApiResponse<String> strResponse = ApiResponse.success("Data processed", "Hello World");
        Assert.assertTrue(strResponse.isSuccess());
        Assert.assertEquals("Hello World", strResponse.getData());

        // Generic response with Double
        ApiResponse<Double> numResponse = ApiResponse.success(540.5);
        Assert.assertEquals(Double.valueOf(540.5), numResponse.getData());

        // Generic response with Collection
        List<String> list = Arrays.asList("Cardio", "Strength", "HIIT");
        ApiResponse<List<String>> listResponse = ApiResponse.success("List loaded", list);
        Assert.assertEquals(3, listResponse.getData().size());
    }

    @Test
    public void testGenericCollectionUtilsFilterAndMap() {
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        // Generic filter
        List<Integer> evens = CollectionUtils.filter(numbers, n -> n % 2 == 0);
        Assert.assertEquals(Arrays.asList(2, 4, 6, 8, 10), evens);

        // Generic map
        List<String> formatted = CollectionUtils.map(evens, n -> "Num: " + n);
        Assert.assertEquals("Num: 2", formatted.get(0));
    }

    @Test
    public void testCustomComparatorSorting() {
        List<ProgressRecord> records = new ArrayList<>();
        records.add(new ProgressRecord(1, 10, 1, "Plan A", 70.0, 30, 250.0, "Easy"));
        records.add(new ProgressRecord(2, 10, 1, "Plan B", 69.8, 45, 480.0, "Hard"));
        records.add(new ProgressRecord(3, 10, 2, "Plan C", 69.5, 40, 350.0, "Moderate"));

        // Custom Comparator: sort by calories burned descending
        Comparator<ProgressRecord> byCaloriesDesc = (r1, r2) ->
                Double.compare(r2.getCaloriesBurned(), r1.getCaloriesBurned());

        List<ProgressRecord> sorted = CollectionUtils.sorted(records, byCaloriesDesc);

        Assert.assertEquals(Double.valueOf(480.0), Double.valueOf(sorted.get(0).getCaloriesBurned()));
        Assert.assertEquals(Double.valueOf(350.0), Double.valueOf(sorted.get(1).getCaloriesBurned()));
        Assert.assertEquals(Double.valueOf(250.0), Double.valueOf(sorted.get(2).getCaloriesBurned()));
    }

    @Test
    public void testStreamGroupingAndAggregation() {
        List<ProgressRecord> records = new ArrayList<>();
        records.add(new ProgressRecord(1, 10, 1, "Strength Accelerator", 70.0, 30, 200.0, ""));
        records.add(new ProgressRecord(2, 10, 1, "Strength Accelerator", 70.0, 40, 300.0, ""));
        records.add(new ProgressRecord(3, 10, 2, "5K Cardio", 69.8, 45, 400.0, ""));

        // Group by plan title and sum calories
        Map<String, Double> caloriesByPlan = records.stream()
                .collect(Collectors.groupingBy(
                        ProgressRecord::getWorkoutPlanTitle,
                        Collectors.summingDouble(ProgressRecord::getCaloriesBurned)
                ));

        Assert.assertEquals(2, caloriesByPlan.size());
        Assert.assertEquals(Double.valueOf(500.0), caloriesByPlan.get("Strength Accelerator"));
        Assert.assertEquals(Double.valueOf(400.0), caloriesByPlan.get("5K Cardio"));
    }
}
