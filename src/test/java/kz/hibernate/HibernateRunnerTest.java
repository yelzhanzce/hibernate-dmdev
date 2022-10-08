package kz.hibernate;

import kz.hibernate.entity.Company;
import kz.hibernate.entity.User;
import kz.hibernate.util.HibernateUtil;
import lombok.Cleanup;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;

import javax.persistence.Column;
import javax.persistence.Table;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

class HibernateRunnerTest {




    @Test
    void lazyInit(){
        Company company;
        try(var factory = HibernateUtil.buildSessionFactory();
            var session = factory.openSession()){
            session.beginTransaction();
            company = session.get(Company.class, 6);
            Hibernate.initialize(company.getUsers());
            session.getTransaction().commit();
        }

        var users = company.getUsers();

        System.out.println(users.size());
    }


    @Test
    void deleteCompany(){
        @Cleanup var factory = HibernateUtil.buildSessionFactory();
        @Cleanup var session = factory.openSession();

        session.beginTransaction();

//        var company = session.get(Company.class, 1);
        var user = session.get(User.class, 2L);
//        session.delete(company);
        session.delete(user);

        session.getTransaction().commit();
    }


    @Test
    void addUserToCompany(){
        @Cleanup var factory = HibernateUtil.buildSessionFactory();
        @Cleanup var session = factory.openSession();

        session.beginTransaction();

        var netflix = Company.builder()
                .name("Amazon")
                .build();

        var user = User.builder()
                .username("pascal@gmail.com")
                .build();

        netflix.addUser(user);

        session.save(netflix);

        session.getTransaction().commit();
    }



    @Test
    void checkOneToMany(){
        @Cleanup var factory = HibernateUtil.buildSessionFactory();
        @Cleanup var session = factory.openSession();

        session.beginTransaction();

        var company = session.get(Company.class, 1);
        System.out.println("");

        session.getTransaction().commit();
    }


    @Test
    void checkGetReflectionApi() throws SQLException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.getString("username");

        Class<User> clazz = User.class;

        Constructor<User> constructor = clazz.getConstructor();

        User user = constructor.newInstance();

        Field username = clazz.getDeclaredField("username");
        username.setAccessible(true);
        username.set(user, resultSet.getString("username"));
    }

    @Test
    void checkReflectionApi() throws SQLException, IllegalAccessException {
        User user = User.builder()
                .username("ivan@gmail.com")
                .build();

        String sql = """
                insert
                into
                %s
                (%s)
                values
                (%s)
                """;
        String tableName = Optional.of(user.getClass().getAnnotation(Table.class))
                .map(tableAnnotation -> tableAnnotation.schema() + "." + tableAnnotation.name())
                .orElse(user.getClass().getName());

        Field[] declaredFields = user.getClass().getDeclaredFields();
        String columnName = Arrays.stream(declaredFields)
                .map(field -> Optional.ofNullable(field.getAnnotation(Column.class))
                        .map(Column::name)
                        .orElse(field.getName()))
                .collect(Collectors.joining(", "));

        String columnValues = Arrays.stream(declaredFields)
                .map(field -> "?")
                .collect(Collectors.joining(", "));

        System.out.println(sql.formatted(tableName, columnName, columnValues));

        Connection connection = null;

        PreparedStatement preparedStatement = connection.prepareStatement(sql.formatted(tableName, columnName, columnValues));

        for (Field declaredField : declaredFields) {
            declaredField.setAccessible(true);
            preparedStatement.setObject(1, declaredField.get(user));
        }
    }

}