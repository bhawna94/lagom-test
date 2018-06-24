package edu.knoldus.api.emp;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Employee {

   int employeeId;
   String employeeName;
   int age;
   String gender;
   int totalDues;
   String lastPaid;
}
