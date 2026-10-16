package com.fitness.service;

import com.fitness.model.Exercise;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing the interactive Exercise and Movement Library.
 * Demonstrates Collections, Generics, and Java Streams API.
 */
public class ExerciseService {

    private final List<Exercise> exerciseLibrary;

    public ExerciseService() {
        this.exerciseLibrary = new ArrayList<>();
        seedExercises();
    }

    private void seedExercises() {
        int id = 1;
        // Strength
        exerciseLibrary.add(new Exercise(id++, "Barbell Back Squat", "Strength", "Quadriceps, Glutes, Hamstrings",
                "Intermediate", "Olympic Barbell & Rack", "Rest bar on upper traps, descend until thighs parallel floor, drive up through midfoot.", 9.5, "🏋️‍♂️"));
        exerciseLibrary.add(new Exercise(id++, "Romanian Deadlift", "Strength", "Hamstrings, Gluteus Maximus, Lower Back",
                "Intermediate", "Barbell or Heavy Dumbbells", "Hinge at hips with soft knees, lower bar to mid-shin, squeeze glutes to stand.", 8.8, "🏋️"));
        exerciseLibrary.add(new Exercise(id++, "Incline Dumbbell Bench Press", "Strength", "Upper Pectoralis, Anterior Deltoids, Triceps",
                "Beginner", "Adjustable Bench & Dumbbells", "Set bench to 30 degrees, press dumbbells upward in a slight arc without clanking.", 7.2, "💪"));
        exerciseLibrary.add(new Exercise(id++, "Overhead Barbell Military Press", "Strength", "Deltoids, Upper Traps, Triceps, Core",
                "Advanced", "Barbell", "Stand tall with glutes braced, press overhead in straight vertical bar path locking out elbows.", 8.0, "⚡"));
        exerciseLibrary.add(new Exercise(id++, "Strict Weighted Pull-Ups", "Strength", "Latissimus Dorsi, Biceps, Rhomboids",
                "Advanced", "Pull-Up Bar & Dip Belt", "Full dead hang, pull chest to bar with elbows driving down and back.", 8.5, "🧗"));
        exerciseLibrary.add(new Exercise(id++, "Bulgarian Split Squat", "Strength", "Quadriceps, Gluteus Medius, Core",
                "Intermediate", "Dumbbells & Flat Bench", "Rear foot elevated on bench, drop back knee towards floor maintaining upright torso.", 8.2, "🦵"));

        // Cardio
        exerciseLibrary.add(new Exercise(id++, "Rowing Ergometer 500m Intervals", "Cardio", "Full Body, Lats, Quads, Core, Heart",
                "Intermediate", "Concept2 Rower", "Leg drive 60%, body lean 20%, arm pull 20%. Maintain sub 1:50 split pace.", 14.0, "🚣"));
        exerciseLibrary.add(new Exercise(id++, "Assault AirBike Max Calorie Sprint", "Cardio", "Cardiovascular System, Quads, Shoulders",
                "Advanced", "AirBike", "Full out 30-sec sprint maintaining maximum RPM with coordinated push-pull arm action.", 16.5, "🚴"));
        exerciseLibrary.add(new Exercise(id++, "Zone 2 Steady-State Aerobic Run", "Cardio", "Heart Muscle, Mitochondrial Density",
                "Beginner", "Treadmill or Outdoor Track", "Maintain conversational pace at 60-70% maximum heart rate for 45 minutes.", 10.0, "🏃"));
        exerciseLibrary.add(new Exercise(id++, "Speed Rope Double Unders", "Cardio", "Calves, Shoulders, Coordination, Heart",
                "Advanced", "Speed Jump Rope", "Two rope revolutions per jump, rotate from wrists while bounding off forefeet.", 13.0, "🪢"));

        // HIIT
        exerciseLibrary.add(new Exercise(id++, "Heavy Battle Rope Slams", "HIIT", "Shoulders, Upper Back, Core, Hip Flexors",
                "Intermediate", "50ft Battle Ropes", "Explode up onto toes and slam ropes simultaneously into the rubber turf with full force.", 12.5, "🌊"));
        exerciseLibrary.add(new Exercise(id++, "Burpee Box Jump Overs", "HIIT", "Full Body Power, Chest, Quads, Explosiveness",
                "Advanced", "24-inch Plyo Box", "Drop into chest-to-deck burpee, snap feet up and bound immediately onto the box.", 14.5, "📦"));
        exerciseLibrary.add(new Exercise(id++, "Kettlebell Swing (Russian / American)", "HIIT", "Posterior Chain, Glutes, Hamstrings, Grip",
                "Intermediate", "24kg/32kg Kettlebell", "Violent hip extension driving the bell upward to chest level without using arms.", 11.0, "🔔"));
        exerciseLibrary.add(new Exercise(id++, "Slam Ball Ground-to-Overhead", "HIIT", "Core, Shoulders, Lats, Glutes",
                "Beginner", "15kg Dead Slam Ball", "Pick ball from ground with flat back, extend overhead, and slam down aggressively.", 11.5, "💥"));

        // Mobility & Yoga
        exerciseLibrary.add(new Exercise(id++, "Vinyasa Sun Salutation Flow", "Mobility / Yoga", "Spine Mobility, Hamstrings, Shoulders, Core",
                "Beginner", "Yoga Mat", "Smooth transition from Mountain Pose to Forward Fold, Chaturanga, Upward Dog, and Downward Dog.", 5.0, "🧘‍♀️"));
        exerciseLibrary.add(new Exercise(id++, "Pigeon Pose Hip Opener", "Mobility / Yoga", "Gluteus Medius, Piriformis, Hip Capsule",
                "Beginner", "Yoga Mat & Blocks", "Extend rear leg straight back, angle front shin diagonally and sink hips evenly.", 3.5, "🕊️"));
        exerciseLibrary.add(new Exercise(id++, "Full Body Foam Roller Myofascial Release", "Mobility / Yoga", "IT Band, Thoracic Spine, Calves, Lats",
                "Beginner", "High-Density Foam Roller", "Slow rolling over trigger points with 30-second sustained pressure on tight tissue.", 4.0, "💆"));
        exerciseLibrary.add(new Exercise(id++, "Thoracic Spine Foam Roller Extensions", "Mobility / Yoga", "Upper Back Mobility, Postural Realignment",
                "Beginner", "Foam Roller", "Support head with hands, hinge upper back over the roller to counter desk slouching.", 3.2, "🧍"));

        // Spa Recovery
        exerciseLibrary.add(new Exercise(id++, "Nordic Contrast Hydrotherapy Soak", "Spa Recovery", "Vascular Flushing, Nervous System Reset",
                "Beginner", "Hot Jacuzzi & 10°C Cold Plunge", "15 mins hot thermal hydro soak followed by 3 mins cold immersion for vasodilation flush.", 4.5, "🛁"));
        exerciseLibrary.add(new Exercise(id++, "Full Body Cryotherapy Session (-110°C)", "Spa Recovery", "Endorphin Surge, Acute Inflammation Reduction",
                "Intermediate", "Sub-Zero Cryo Chamber", "3-minute whole-body cryotherapy session with regulated sub-zero air circulation.", 5.5, "❄️"));
        exerciseLibrary.add(new Exercise(id++, "Deep Tissue Swedish Massage & Cupping", "Spa Recovery", "Deep Fascia, Muscle Knots, Lactic Clearance",
                "Beginner", "Therapy Suite & Organic Oils", "60-minute targeted deep pressure therapy with heated basalt stones and myofascial cupping.", 3.0, "💆‍♂️"));
    }

    public List<Exercise> getAllExercises() {
        return Collections.unmodifiableList(exerciseLibrary);
    }

    public List<Exercise> filter(String query, String category, String difficulty) {
        return exerciseLibrary.stream()
                .filter(e -> query == null || query.trim().isEmpty() ||
                        e.getName().toLowerCase().contains(query.toLowerCase().trim()) ||
                        e.getTargetMuscles().toLowerCase().contains(query.toLowerCase().trim()))
                .filter(e -> category == null || category.trim().isEmpty() || "ALL".equalsIgnoreCase(category) ||
                        e.getCategory().equalsIgnoreCase(category.trim()))
                .filter(e -> difficulty == null || difficulty.trim().isEmpty() || "ALL".equalsIgnoreCase(difficulty) ||
                        e.getDifficulty().equalsIgnoreCase(difficulty.trim()))
                .collect(Collectors.toList());
    }
}
