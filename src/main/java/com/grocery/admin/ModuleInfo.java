package com.grocery.admin;

/**
 * One card on the admin dashboard = one team member's module.
 * "path" is how we detect the module, "link" is where the card opens.
 * "built" becomes true by itself as soon as the owner's controller exists in the code.
 */
public record ModuleInfo(String name, String icon, String path, String link, String owner,
                         String description, boolean built) {
}
