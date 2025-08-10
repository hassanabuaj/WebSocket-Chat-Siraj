package com.assignmenthasan.chatapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.convert.NoOpDbRefResolver;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

/**
 * תצורת MongoDB מותאמת אישית
 * מגדירה ממיר שלא מוסיף שדה _class למסמכי MongoDB
 */
@Configuration
public class MongoConfig {

    /**
     * יוצרת ממיר MongoDB מותאם שלא מוסיף שדה _class
     * זה מבטיח שמסמכי MongoDB יהיו נקיים ויתאימו לדרישות המטלה
     * @param context הקשר מיפוי MongoDB
     * @param conversions המרות מותאמות אישית
     * @return ממיר MongoDB מותאם
     */
    @Bean
    public MappingMongoConverter mappingMongoConverter(MongoMappingContext context,
                                                       MongoCustomConversions conversions) {
        MappingMongoConverter converter = new MappingMongoConverter(
                NoOpDbRefResolver.INSTANCE, context);
        converter.setCustomConversions(conversions);
        
        // Remove the _class field from MongoDB documents
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        
        return converter;
    }
}
