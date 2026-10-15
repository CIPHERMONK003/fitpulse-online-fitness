package com.fitness.service;

import com.fitness.concurrency.NotificationQueueService;
import com.fitness.concurrency.NotificationTask;
import com.fitness.dao.CoachFeedbackDao;
import com.fitness.dao.UserDao;
import com.fitness.exception.UserNotFoundException;
import com.fitness.model.CoachFeedback;
import com.fitness.model.User;

import java.util.List;

/**
 * Service managing Coach-to-Client communications and feedback.
 */
public class CoachFeedbackService {

    private final CoachFeedbackDao feedbackDao;
    private final UserDao userDao;
    private final NotificationQueueService notificationQueue;

    public CoachFeedbackService() {
        this.feedbackDao = new CoachFeedbackDao();
        this.userDao = new UserDao();
        this.notificationQueue = NotificationQueueService.getInstance();
    }

    public CoachFeedbackService(CoachFeedbackDao feedbackDao, UserDao userDao, NotificationQueueService notificationQueue) {
        this.feedbackDao = feedbackDao;
        this.userDao = userDao;
        this.notificationQueue = notificationQueue;
    }

    public CoachFeedback sendFeedback(int coachId, int clientId, String message, String planRecommendation, int rating)
            throws UserNotFoundException {

        User coach = userDao.findById(coachId)
                .orElseThrow(() -> new UserNotFoundException(coachId));
        User client = userDao.findById(clientId)
                .orElseThrow(() -> new UserNotFoundException(clientId));

        CoachFeedback feedback = new CoachFeedback(
                null, coachId, coach.getName(), clientId, client.getName(),
                message, planRecommendation, rating
        );

        CoachFeedback saved = feedbackDao.create(feedback);

        // Multithreaded asynchronous alert to client
        notificationQueue.enqueue(new NotificationTask(
                client.getEmail(), client.getName(),
                "New Feedback from Coach " + coach.getName(),
                "Your coach posted feedback: \"" + message + "\"",
                "IN_APP"
        ));

        return saved;
    }

    public List<CoachFeedback> getFeedbackForClient(int clientId) {
        return feedbackDao.findByClientId(clientId);
    }

    public List<CoachFeedback> getFeedbackByCoach(int coachId) {
        return feedbackDao.findByCoachId(coachId);
    }
}
