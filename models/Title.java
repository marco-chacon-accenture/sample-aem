/*
 *  Copyright 2015 Adobe Systems Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.accenture.core.models;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import com.day.cq.commons.jcr.JcrConstants;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Via;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.apache.sling.settings.SlingSettingsService;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

import java.util.Optional;

import static org.apache.sling.api.resource.ResourceResolver.PROPERTY_RESOURCE_TYPE;

@Model(adaptables = {Resource.class, SlingHttpServletRequest.class, }, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class Title {

    @Inject
    @Named(PROPERTY_RESOURCE_TYPE)
    @Via("resource")
    @Default(values="No resourceType")
    protected String resourceType;

    @SlingObject
    private Resource resource;

    @OSGiService
    private SlingSettingsService settings;

    //The resource this general used to get Resources on AEM
    @SlingObject
    private ResourceResolver resourceResolver;

    // The SlingHttpServletRequest allows us to get access for example to i18n and is required for some method in
    // in the AEM Backend we are not using it right now but you will know how to get one
    @Inject
    private SlingHttpServletRequest request;

    // we can inject the current page because this model is adaptable from SlingHttpServletRequest
    @Inject
    private Page currentPage;

    private String componentInfo;

    @Inject
    @ValueMapValue
    @Named(JcrConstants.JCR_TITLE)
    private String title;

    @Inject
    @Named("type")
    private String headingLevel;

    private String alignment;

    @PostConstruct
    protected void init() {
        ValueMap pageProperties = currentPage.adaptTo(ValueMap.class);

        PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
        String currentPagePath = Optional.ofNullable(pageManager)
                .map(pm -> pm.getContainingPage(resource))
                .map(Page::getPath).orElse("");

        componentInfo = "Title Component !\n"
            + "Resource type is: " + resourceType + "\n"
            + "Current page is:  " + currentPagePath + "\n"
            + "Page Title: " + currentPage.getTitle() + "\n"
            + "Page Created By: " + pageProperties.get(JcrConstants.JCR_CREATED_BY,String.class) + "\n"
            + "This is instance: " + settings.getSlingId() + "\n";

        // option 1= Get the alignment value from the ValueMap
        ValueMap valueMap = resource.getValueMap();
        String alignment = valueMap.get("alignment", String.class);

        //option 2 = adapt the Resource to a Value map and then get the value
        alignment = resource.adaptTo(ValueMap.class).get("alignment", String.class);
        this.alignment = alignment;

    }

    public String getComponentInfo() {
        return componentInfo;
    }

    public String getText() {
        return title != null ? title : currentPage.getTitle();
    }

    public String getHeadingLevel() {
        return headingLevel;
    }

    public String getAlignment() {
        return alignment;
    }
}
