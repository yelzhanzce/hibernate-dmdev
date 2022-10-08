package kz.hibernate;

import kz.hibernate.entity.*;
import kz.hibernate.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDate;

public class HibernateRunner {
    private static Logger log = LoggerFactory.getLogger(HibernateRunner.class);

    public static void main(String[] args) throws SQLException {
//        Connection connection = DriverManager
//                .getConnection("db.url", "db.username", "db.password");




        try (SessionFactory sessionFactory = HibernateUtil.buildSessionFactory();
             Session session = sessionFactory.openSession()) {

            session.beginTransaction();
            
            Company company = Company.builder()
                    .name("Meta")
                    .build();

            User user = User.builder()
                    .username("john2@gmail.com")
                    .personalInfo(PersonalInfo.builder()
                            .firstname("John")
                            .lastname("Smith")
                            .birthDate(new Birthday(LocalDate.of(2000, 1, 19)))
                            .build())
                    .company(company)
                    .role(Role.Admin)
                    .build();

            session.save(user);
//            session.saveOrUpdate(user);
//            session.delete(user);
//            session.update(user);

            session.getTransaction().commit();
        }

    }
}
