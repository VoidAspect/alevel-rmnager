package com.alevel.rmanager.data.service.jpa;

import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

abstract class JPATest {

    Session session;

    static SessionFactory sessionFactory;

    static ValidatorFactory validatorFactory;

    @BeforeAll
    static void setupFactories() {
        var config = new Configuration().configure();
        validatorFactory = Validation.buildDefaultValidatorFactory();
        sessionFactory = config.buildSessionFactory();
    }

    @BeforeEach
    void setupSession() {
        session = sessionFactory.openSession();
    }

    @AfterEach
    void closeSession() {
        session.close();
    }

    @AfterAll
    static void closeFactories() {
        validatorFactory.close();
        sessionFactory.close();
    }
}
