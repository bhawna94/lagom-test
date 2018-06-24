package edu.knoldus.api.emp;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.transport.Method.DELETE;
import static com.lightbend.lagom.javadsl.api.transport.Method.GET;
import static com.lightbend.lagom.javadsl.api.transport.Method.POST;
import static com.lightbend.lagom.javadsl.api.transport.Method.PUT;


public interface EmployeeService extends Service {

    ServiceCall<NotUsed, Employee> getEmployeeDetail(int employeeId);

    ServiceCall<Employee, String> postEmployeeDetail();

    ServiceCall<NotUsed, String> deleteEmployeeDetail(int employeeId);



    @Override
    default Descriptor descriptor() {
        return named("employee-service").withCalls(
                Service.restCall(GET, "/api/employeeDetails/:employeeId", this::getEmployeeDetail),
                Service.restCall(POST, "/api/addEmployee", this::postEmployeeDetail),
                Service.restCall(DELETE,"/api/removeEmployee/:employeeId", this::deleteEmployeeDetail)

        ).withAutoAcl(true);

    }
}
