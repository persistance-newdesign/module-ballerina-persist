// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/time;

@Entity {
    key: ["needId"]
}
public type MedicalNeed record {|
    @AutoIncrement
    readonly int needId = -1;

    int itemId;
    int beneficiaryId;
    time:Civil period;
    string urgency;
    int quantity;
|};

@Entity {
    key: ["itemId"]
}
public type MedicalItem record {|
    readonly int itemId;
    string name;
    string 'type;
    string unit;
|};

@Entity {
    key: ["complexTypeId"]
}
public type ComplexType record {|
    @AutoIncrement
    readonly int complexTypeId = -1;
    time:Civil civilType;
    time:TimeOfDay timeOfDayType;
    time:Date dateType;
|};

@Entity {
    key: ["hospitalCode", "departmentId"]
}
public type Department record {|
    string hospitalCode;
    int departmentId;
    string name;
|};

// One-to-one relation
@Entity {
    key: ["id"]
}
public type User record {|
    readonly int id;
    string name;
    Profile profile?;
|};

@Entity {
    key: ["id"]
}
public type Profile record {|
    readonly int id;
    string name;
    @Relation {keyColumns: ["userId"], reference: ["id"]}
    User user?;
|};

@Entity {
    key: ["id"]
}
public type MultipleAssociations record {|
    readonly int id;
    string name;

    @Relation {keyColumns: ["profileId"], reference: ["id"]}
    Profile profile?;

    @Relation {keyColumns: ["userId"], reference: ["id"]}
    User user?;
|};

// One-to-many relation
@Entity {
    key: ["id"]
}
public type Company record {|
    readonly int id;
    string name;
    Employee[] employees?;
|};

@Entity {
    key: ["id"]
}
public type Employee record {|
    readonly int id;
    string name;

    @Relation {keyColumns: ["companyId"], reference: ["id"]}
    Company company?;
|};


// Many-to-many relation
@Entity {
    key: ["studentId"],
    tableName: "Students"
}
public type Student record {|
    int o_studentId;
    string o_firstName;
    string o_lastName;
    time:Date o_dob;
    string o_contact;

    @Relation {
        keyColumns: ["studentId"],
        reference: ["o_lectureId"],
        joiningTable: {
            name: "StudentsLectures",
            lhsColumns: ["i_studentId"],
            rhsColumns: ["i_lectureId"]
        }
    }
    Lecture[] o_lectures?;

    @Relation {
        keyColumns: ["studentId"], 
        reference: ["o_subjectId", "o_date"],
        joiningTable: {
            name: "StudentsPapers",
            lhsColumns: ["i_studentId"],
            rhsColumns: ["i_subjectId", "i_date"]
        }
    }
    Paper[] o_papers?;
|};

@Entity {
    key: ["lectureId"],
    tableName: "Lectures"
}
public type Lecture record {|
    int o_lectureId;
    string o_subject;
    string o_day;
    time:TimeOfDay o_time;

    @Relation {
        keyColumns: ["lectureId"], 
        reference: ["lectureId"],
        joiningTable: {
            name: "StudentsLectures",
            lhsColumns: ["i_lectureId"],
            rhsColumns: ["i_studentId"]
        }
    }
    Student[] o_students?;
|};

@Entity {
    key: ["o_subjectId", "o_date"],
    tableName: "Papers"
}
public type Paper record {|
    int o_subjectId;
    time:Date o_date;
    string o_title;
    
    @Relation {
        keyColumns: ["studentId"], 
        reference: ["o_subjectId", "o_date"], 
        joiningTable: {
            name: "StudentsPapers", 
            lhsColumns: ["i_subjectId", "i_date"], 
            rhsColumns: ["i_studentId"]
        }
    }
    Student[] o_students?;
|};
