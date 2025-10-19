    package com.example.inventory.repository;

    import com.example.inventory.dto.store.request.StoreUserFindRequest;
    import com.example.inventory.entity.product.Product;
    import com.example.inventory.entity.store.Amenities;
    import com.example.inventory.entity.store.Category;
    import com.example.inventory.entity.store.Store;
    import com.querydsl.core.BooleanBuilder;
    import com.querydsl.core.types.Predicate;
    import com.querydsl.core.types.dsl.BooleanExpression;
    import com.querydsl.jpa.impl.JPAQueryFactory;
    import lombok.RequiredArgsConstructor;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.PageImpl;
    import org.springframework.data.domain.Pageable;
    import org.springframework.stereotype.Repository;
    import org.springframework.transaction.annotation.Transactional;

    import java.util.List;
    import java.util.Map;
    import java.util.Optional;
    import java.util.stream.Collectors;

    import static com.example.inventory.entity.product.QProduct.product;
    import static com.example.inventory.entity.store.QStore.store;
    import static com.example.inventory.util.QueryDslUtils.combineAnd;

    @Repository
    @RequiredArgsConstructor
    public class StoreQueryDslRepositoryImpl implements StoreQueryDslRepository {

        private final JPAQueryFactory jpaQueryFactory;

        @Transactional(readOnly = true)
        @Override
        public Page<Store> findAllByFetchJoin(StoreUserFindRequest request, Pageable pageable) {
            BooleanExpression whereCondition = getBooleanExpression(request);

            List<Store> stores = jpaQueryFactory
                    .select(store)
                    .from(store)
                    .leftJoin(store.amenities).fetchJoin()
                    .where(whereCondition)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .distinct()
                    .fetch();

            List<Product> products = getProducts(stores);

            mapProductsToStores(stores, products);

            Long total = jpaQueryFactory
                    .select(store.countDistinct()) // DISTINCT를 포함한 COUNT
                    .from(store)
                    .where(whereCondition)
                    .fetchOne();

            return new PageImpl<>(stores, pageable, Optional.ofNullable(total).orElse(0L));
        }

        private BooleanExpression getBooleanExpression(StoreUserFindRequest request) {
            BooleanExpression nameCondition = nameLike(request.getName());
            BooleanExpression categoryCondition = categoryEqual(request.getCategory());
            BooleanExpression addressCondition = addressEqual(request.getProvince(), request.getCity());
            BooleanExpression amenitiesCondition = amenitiesAllMatch(request.getAmenities());

            return combineAnd(
                    nameCondition,
                    categoryCondition,
                    addressCondition,
                    amenitiesCondition
            );
        }

        private List<Product> getProducts(List<Store> stores) {
            List<Long> storeIds = stores.stream()
                    .map(Store::getId)
                    .toList();

            return jpaQueryFactory.select(product)
                    .from(product)
                    .where(product.store.id.in(storeIds))
                    .fetch();
        }

        private void mapProductsToStores(List<Store> stores, List<Product> products) {
            Map<Long, List<Product>> productMap = products.stream()
                    .collect(Collectors.groupingBy(p -> p.getStore().getId()));

            stores.forEach(store -> {
                List<Product> productList = productMap.get(store.getId());
                store.assignProducts(productList);
            });
        }

        private BooleanExpression nameLike(String name) {
            if (name == null || name.isEmpty()) {
                return null;
            }

            String searchName = "%" + name + "%";
            return store.name.like(searchName);
        }

        private BooleanExpression categoryEqual(String category) {
            if (category == null || category.isEmpty()) {
                return null;
            }

            Category categoryName;

            try {
                categoryName = Category.valueOf(category);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("존재하지 않는 카테고리 입니다.");
            }

            return store.category.eq(categoryName);
        }

        private BooleanExpression addressEqual(String province, String city) {
            if (province == null || province.isEmpty()) {
                return null;
            }

            if(city == null || city.isEmpty()) {
                return null;
            }

            return store.address.province.eq(province)
                    .and(store.address.city.eq(city));
        }

        private BooleanExpression amenitiesAllMatch(List<String> amenities) {
            if(amenities == null || amenities.isEmpty()) {
                return null;
            }

            BooleanExpression result = null;

            for(String amenity : amenities) {
                try {
                    Amenities amenityEnum = Amenities.valueOf(amenity);

                    BooleanExpression currentCondition = store.amenities.contains(amenityEnum);

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
