import ballerina/sql;

public type FieldMetadata record {|
    string columnName;
    typedesc 'type;
    boolean autoGenerated = false;
|};

public type FilterQuery distinct sql:ParameterizedQuery;
