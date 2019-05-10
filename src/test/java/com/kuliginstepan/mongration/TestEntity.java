package com.kuliginstepan.mongration;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "entity")
@Data
public class TestEntity {

    @Id
    private String id;
    @Indexed(unique = true)
    private String index;
    private String text;

}
