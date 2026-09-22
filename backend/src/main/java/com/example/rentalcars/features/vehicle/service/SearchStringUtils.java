package com.example.rentalcars.features.vehicle.service;


public final class SearchStringUtils {

    public static String cleanSearchParam(String search) {
        if (search == null || search.trim().isEmpty()) {
            return null;
        }
        return "%" + search.trim().toLowerCase() + "%";
    }

    public static String cleanBrandParam(String brand) {
        if (brand == null || brand.trim().isEmpty()) {
            return null;
        }
        return brand.trim().toLowerCase();
    }
}