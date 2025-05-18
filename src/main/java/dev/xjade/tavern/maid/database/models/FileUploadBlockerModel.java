package dev.xjade.tavern.maid.database.models;

import java.util.List;

public record FileUploadBlockerModel(
    boolean enabled, List<String> allowedExtensions, long minRole) {}
