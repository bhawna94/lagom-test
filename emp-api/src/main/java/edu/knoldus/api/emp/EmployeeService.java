package edu.knoldus.api.emp;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.transport.Method.GET;

public interface EmployeeService extends Service {

    ServiceCall<NotUsed, Employee> getEmployeeDetail(int employeeId);


    @Override
    default Descriptor descriptor() {
        return named("employee-service").withCalls(
                Service.restCall(GET, "/api/employeeDetails/:employeeId", this::getEmployeeDetail)
        )
                        .withAutoAcl(true);
    }
}
