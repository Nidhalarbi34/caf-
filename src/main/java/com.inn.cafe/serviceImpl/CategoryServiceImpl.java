package com.inn.cafe.serviceImpl;

import com.google.common.base.Strings;
import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.POJO.Category;
import com.inn.cafe.Repo.CategoryRepository;
import com.inn.cafe.constents.CafeConstants;
import com.inn.cafe.dao.CategoryDao;
import com.inn.cafe.service.CategoryService;
import com.inn.cafe.utils.CafeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;


@Slf4j
@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    CategoryDao categoryDao;
    @Autowired
    JwtFilter jwtFilter;
    @Override
    public ResponseEntity<String> addNewCategory(Map<String, String> requestMap) {
        try {
            if (validateCategoryMap(requestMap,true)) {
                categoryDao.save(getCategoryFromMap(requestMap,true));
                return CafeUtils.getResponeEntity("Category Added successfully", HttpStatus.OK);
            }
        } catch (Exception ex) {
            log.error("Error occurred while adding new category: {}", ex.getMessage(), ex);
        }
        return CafeUtils.getResponeEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Category>> getAllCategory(String filterValue) {
        try {
            if (!Strings.isNullOrEmpty(filterValue) && filterValue.equalsIgnoreCase("true")) {
                log.info("Inside if");
                return new ResponseEntity<List<Category>>(new ArrayList<>(), HttpStatus.OK);
            }
            return new ResponseEntity<>(categoryDao.findAll(), HttpStatus.OK);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // In case of an error, return an empty list with HTTP 500 Internal Server Error
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> update(Map<String, String> requestMap) {
        try {
            if (jwtFilter.isAdmin()) {
                if (validateCategoryMap(requestMap , true)) {

                    Optional optional = categoryDao.findById(Integer.parseInt(requestMap.get("id")));

                    if (!optional.isEmpty()) {
                        categoryDao.save(getCategoryFromMap(requestMap,true));
                        return CafeUtils.getResponeEntity("Category is updated successfully", HttpStatus.OK);

                    } else {
                        return CafeUtils.getResponeEntity("Category id doesn't exist", HttpStatus.OK);
                    }

                }
                return CafeUtils.getResponeEntity(CafeConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            } else {
                return CafeUtils.getResponeEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return CafeUtils.getResponeEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    private boolean validateCategoryMap(Map<String, String> requestMap, boolean validateId) {
        if (requestMap.containsKey("name")){
            if (requestMap.containsKey("id")&& validateId){
                return true;
            } else if (!validateId) {
                return true;

            }
        }
        return false;
    }

    private Category getCategoryFromMap(Map<String, String> requestMap, Boolean isAdd) {
        Category category = new Category();
        category.setName(requestMap.get("name"));
        if (isAdd) {
            // Check if "id" is present in the request
            String idStr = requestMap.get("id");
            if (!Objects.equals(idStr, "0") && !idStr.isEmpty()) {
                ///updateeeeeeeeeee
                category.setId(Integer.parseInt(requestMap.get("id")));
            }
        }
        return category;
    }


}
