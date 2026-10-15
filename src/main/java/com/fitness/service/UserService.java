package com.fitness.service;

import com.fitness.concurrency.NotificationQueueService;
import com.fitness.concurrency.NotificationTask;
import com.fitness.dao.UserDao;
import com.fitness.exception.AuthenticationException;
import com.fitness.exception.UserAlreadyExistsException;
import com.fitness.exception.UserNotFoundException;
import com.fitness.model.*;
import com.fitness.util.PasswordUtil;

import java.util.List;
import java.util.Optional;

/**
 * Service managing user authentication, registration, profiles, and coach assignments.
 * Demonstrates:
 * - Business logic layer
 * - Custom checked Exception handling
 * - Polymorphism in User instances
 * - Multithreaded notification enqueueing
 */
public class UserService {

    private final UserDao userDao;
    private final NotificationQueueService notificationQueue;

    public UserService() {
        this.userDao = new UserDao();
        this.notificationQueue = NotificationQueueService.getInstance();
    }

    public UserService(UserDao userDao, NotificationQueueService notificationQueue) {
        this.userDao = userDao;
        this.notificationQueue = notificationQueue;
    }

    /**
     * Authenticates user by email and password.
     * Throws custom AuthenticationException on failure.
     */
    public User authenticate(String email, String plainPassword) throws AuthenticationException {
        if (email == null || email.trim().isEmpty() || plainPassword == null || plainPassword.isEmpty()) {
            throw new AuthenticationException("Email and password are required.");
        }

        Optional<User> optUser = userDao.findByEmail(email.trim().toLowerCase());
        if (optUser.isEmpty()) {
            throw new AuthenticationException("Invalid credentials: user not found.");
        }

        User user = optUser.get();
        if (!user.isActive()) {
            throw new AuthenticationException("Account is deactivated. Please contact an administrator.");
        }

        if (!PasswordUtil.verifyPassword(plainPassword, user.getPasswordHash())) {
            throw new AuthenticationException("Invalid credentials: password does not match.");
        }

        return user;
    }

    /**
     * Registers a new client account.
     */
    public ClientUser registerClient(String name, String email, String plainPassword,
                                    String fitnessGoal, double currentWeightKg, double targetWeightKg,
                                    double heightCm, Integer coachId, String activityLevel)
            throws UserAlreadyExistsException {

        if (userDao.findByEmail(email.trim().toLowerCase()).isPresent()) {
            throw new UserAlreadyExistsException(email);
        }

        String passwordHash = PasswordUtil.hashPassword(plainPassword);
        ClientUser client = new ClientUser(
                null, name, email.trim().toLowerCase(), passwordHash,
                fitnessGoal, currentWeightKg, targetWeightKg, heightCm, coachId, activityLevel
        );

        userDao.create(client);

        // Multithreaded asynchronous welcome notification
        notificationQueue.enqueue(new NotificationTask(
                client.getEmail(), client.getName(),
                "Welcome to Online Fitness Coaching Platform!",
                "Hello " + client.getName() + ", your fitness journey starts today! Explore workout plans and connect with your coach.",
                "EMAIL_SIMULATION"
        ));

        return client;
    }

    /**
     * Registers a new coach account.
     */
    public CoachUser registerCoach(String name, String email, String plainPassword,
                                   String specialization, String certification,
                                   int experienceYears, String bio)
            throws UserAlreadyExistsException {

        if (userDao.findByEmail(email.trim().toLowerCase()).isPresent()) {
            throw new UserAlreadyExistsException(email);
        }

        String passwordHash = PasswordUtil.hashPassword(plainPassword);
        CoachUser coach = new CoachUser(
                null, name, email.trim().toLowerCase(), passwordHash,
                specialization, certification, experienceYears, 5.0, bio
        );

        userDao.create(coach);

        notificationQueue.enqueue(new NotificationTask(
                coach.getEmail(), coach.getName(),
                "Welcome Coach to the Platform!",
                "You are now registered as an accredited coach. You can create workout plans and guide clients.",
                "EMAIL_SIMULATION"
        ));

        return coach;
    }

    public User getUserById(int userId) throws UserNotFoundException {
        return userDao.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    public List<CoachUser> getAllCoaches() {
        return userDao.findAllCoaches();
    }

    public List<ClientUser> getClientsForCoach(int coachId) {
        return userDao.findClientsByCoachId(coachId);
    }

    public List<User> getAllUsers() {
        return userDao.findAll();
    }

    public void assignCoach(int clientId, int coachId) throws UserNotFoundException {
        User client = getUserById(clientId);
        User coach = getUserById(coachId);

        userDao.assignCoachToClient(clientId, coachId);

        // Notify client asynchronously
        notificationQueue.enqueue(new NotificationTask(
                client.getEmail(), client.getName(),
                "New Coach Assigned!",
                "Coach " + coach.getName() + " has been assigned to guide your workout journey.",
                "IN_APP"
        ));
    }

    public void updateWeight(int clientId, double newWeightKg) {
        userDao.updateClientWeight(clientId, newWeightKg);
    }
}
