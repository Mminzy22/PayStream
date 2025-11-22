package com.paystream.inventory.store.repository;

import static com.paystream.inventory.store.entity.QStore.store;

import com.paystream.inventory.store.entity.Amenities;
import com.paystream.inventory.store.entity.Category;
import com.querydsl.core.types.dsl.BooleanExpression;
import java.util.List;

public class StorePredicate {

    public static BooleanExpression nameLike(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }

        String searchName = "%" + name + "%";
        return store.name.like(searchName);
    }

    public static BooleanExpression categoryEqual(Category category) {
        if (category == null) {
            return null;
        }

        return store.category.eq(category);
    }

    public static BooleanExpression addressEqual(String province, String city) {
        if (province == null || province.isEmpty()) {
            return null;
        }

        if (city == null || city.isEmpty()) {
            return null;
        }

        return store.address.province.eq(province).and(store.address.city.eq(city));
    }

    public static BooleanExpression amenitiesAllMatch(List<Amenities> amenities) {
        if (amenities == null || amenities.isEmpty()) {
            return null;
        }

        BooleanExpression result = null;

        for (Amenities amenity : amenities) {
            try {
                BooleanExpression currentCondition = store.amenities.contains(amenity);

                if (result == null) {
                    result = currentCondition;
                } else {
                    result = result.and(currentCondition);
                }
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid amenity: " + amenity);
            }
        }

        return result;
    }
}
