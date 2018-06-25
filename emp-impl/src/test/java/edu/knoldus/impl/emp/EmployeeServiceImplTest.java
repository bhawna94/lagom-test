package edu.knoldus.impl.emp;

import com.datastax.driver.core.Session;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;
import com.lightbend.lagom.javadsl.testkit.ServiceTest;
import edu.knoldus.api.emp.Employee;
import edu.knoldus.api.emp.EmployeeService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.defaultSetup;
import static com.lightbend.lagom.javadsl.testkit.ServiceTest.startServer;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class EmployeeServiceImplTest {

    private static ServiceTest.TestServer server;
    private static EmployeeServiceImpl employeeServiceImpl;

    @BeforeClass
    public static void setUp() throws Exception {

        final ServiceTest.Setup setup = defaultSetup();

        server = startServer(setup.withCassandra(true));

        CassandraSession cassandraSession = server.injector().instanceOf(CassandraSession.class);

        Session session = cassandraSession.underlying().toCompletableFuture().get();

        createSchema(session);

        employeeServiceImpl = new EmployeeServiceImpl(cassandraSession);


    }

    public static void createSchema(Session session) {
        session.execute("CREATE KEYSPACE lagom_test WITH replication = {'class': 'SimpleStrategy', 'replication_factor':1};");
        session.execute("CREATE TABLE lagom_test.employee(e_id int PRIMARY KEY, age int, gender text, last_paid text, name text, total_dues int);");
        session.execute("insert into lagom_test.employee(e_id,age,gender,last_paid,name,total_dues) values(1,23,'Female','25-02-2018','bhawna',500);");
    }

    @AfterClass

    public static void tearDown() {
        if (server != null) {
            server.stop();
            server = null;
        }
    }

    EmployeeService employeeService = server.client(EmployeeService.class);


    @Test
    public void fetchEmployeeDetail() throws Exception {
        Employee employee = employeeService.getEmployeeDetail(1).invoke().toCompletableFuture().get(5, TimeUnit.SECONDS);
        assertEquals(Employee.builder().employeeId(1).age(23).gender("Female").lastPaid("25-02-2018").employeeName("bhawna").totalDues(500).build(), employee);
    }

    @Test
    public void fetchEmployeeDetailFailureCase() throws Exception {
        try {
            employeeService.getEmployeeDetail(10).invoke().toCompletableFuture().get(5, TimeUnit.SECONDS);
        } catch (ExecutionException ex) {
            assertTrue(ex.getMessage().contains("com.lightbend.lagom.javadsl.api.transport.NotFound"));
        }
    }

    @Test
    public void postEmployeeWhenUserDoesNotExist() throws Exception {
        String message = employeeService.postEmployeeDetail()
                .invoke(Employee.builder()
                        .employeeId(4)
                        .age(25)
                        .gender("Female")
                        .lastPaid("25-08-2018")
                        .employeeName("Ayush")
                        .totalDues(500)
                        .build()).toCompletableFuture().get(5, TimeUnit.SECONDS);
        assertEquals("inserted", message);
    }

    @Test
    public void postEmployeeWhenUserAlreadyExist() throws Exception {
        String message = employeeService.postEmployeeDetail().invoke(Employee.builder()
                .employeeId(1)
                .age(25)
                .gender("Male")
                .lastPaid("25-08-2018")
                .employeeName("Ayush")
                .totalDues(500)
                .build()).toCompletableFuture().get(5, TimeUnit.SECONDS);
        assertEquals("user already exist", message);
    }

    @Test
    public void deleteEmployeeWhenUserExist() throws Exception {

    String message = employeeServiceImpl.deleteUser(Arrays.asList(1,2,3,4),1)
                                      .toCompletableFuture().get(5,TimeUnit.SECONDS);
    assertEquals("user deleted", message);
    }

    @Test
    public void deleteEmployeeWhenUserNotExist() throws Exception{
        String message = employeeServiceImpl.deleteUser(Arrays.asList(1,2,3,4),5)
                            .toCompletableFuture().get(5,TimeUnit.SECONDS);
        assertEquals("user does not exist",message);
    }

    @Test
    public void updateEmployeeWhenUserExist() throws Exception{
        String message = employeeServiceImpl.updateUser(Arrays.asList(1,2,3,4),1,200)
                .toCompletableFuture().get(5,TimeUnit.SECONDS);
        assertEquals("total dues updated", message);
    }

    @Test
    public void updateEmployeeWhenUserDoesNotExist() throws Exception{
        String message = employeeServiceImpl.updateUser(Arrays.asList(1,2,3,4),6,200)
                .toCompletableFuture().get(5,TimeUnit.SECONDS);
        assertEquals("No employee exists", message);
    }

    @Test
    public void serviceDeleteEmployeeWhenUserExist() throws Exception{
        String message = employeeService.deleteEmployeeDetail(1).invoke()
                .toCompletableFuture().get(5,TimeUnit.SECONDS);
        assertEquals("user deleted",message);

    }
    @Test
    public void serviceDeleteEmployeeWhenUserNotExist() throws Exception{
        String message = employeeService.deleteEmployeeDetail(100).invoke()
                .toCompletableFuture().get(5,TimeUnit.SECONDS);
        assertEquals("user does not exist",message);

    }


    @Test
    public void serviceUpdateEmployeeWhenUserExist() throws Exception{
        String message = employeeService.updateEmployeeDetail(1,500).invoke()
                .toCompletableFuture().get(5,TimeUnit.SECONDS);
        assertEquals("total dues updated",message);

    }
    @Test
    public void serviceUpdateEmployeeWhenUserNotExist() throws Exception{
        String message = employeeService.updateEmployeeDetail(100, 500).invoke()
                .toCompletableFuture().get(5,TimeUnit.SECONDS);
        assertEquals("No employee exists",message);

    }


}
