/*
 * Copyright (c) 2013 Goran Ehrsson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package grails.plugins.crm.classified

import grails.events.Listener
import grails.plugins.crm.core.SearchUtils
import grails.plugins.crm.core.TenantUtils
import grails.plugins.selection.Selectable
import org.codehaus.groovy.grails.web.metaclass.BindDynamicMethod

/**
 * Classified Advertisement Service.
 */
class CrmClassifiedService {

    def grailsApplication
    def crmCoreService
    def crmTagService

    @Listener(namespace = "crmClassified", topic = "enableFeature")
    def enableFeature(event) {
        // event = [feature: feature, tenant: tenant, role:role, expires:expires]
        def tenant = event.tenant
        TenantUtils.withTenant(tenant) {
            crmTagService.createTag(name: CrmClassified.name, multiple: true)
        }
    }

    CrmClassified createClassified(Map params, boolean save = false) {
        def reference = params.reference
        if (reference != null && crmCoreService.isDomainClass(reference) && !reference.ident()) {
            throw new RuntimeException(
                    "You must save the domain instance [$reference] before calling createClassified")
        }
        def tenant = TenantUtils.tenant
        def m = CrmClassified.findBySubjectAndTenantId(params.subject, tenant)
        if (!m) {
            m = new CrmClassified()
            def args = [m, params, [include: CrmClassified.BIND_WHITELIST]]
            new BindDynamicMethod().invoke(m, 'bind', args.toArray())
            m.tenantId = tenant
            if (params.status == null) {
                m.status = CrmClassified.STATUS_DRAFT
            }
            if (reference) {
                m.ref = crmCoreService.getReferenceIdentifier(reference)
            }
            if (save) {
                m.save()
            } else {
                m.validate()
                m.clearErrors()
            }
        }
        return m
    }

    def list() {
        list([:], [:])
    }

    @Selectable
    def list(Map<String, Object> params) {
        list([:], params)
    }

    @Selectable
    def list(Map<String, Object> query, Map<String, Object> params) {
        def tagged
        if (query.tags) {
            tagged = crmTagService.findAllIdByTag(CrmClassified, query.tags) ?: [0L]
        }

        def result = CrmClassified.createCriteria().list(params) {
            eq('tenantId', TenantUtils.tenant)
            if (tagged) {
                inList('id', tagged)
            }
            if (query.location) {
                eq('location', query.location)
            }
            if (query.status != null) {
                eq('status', query.status)
            }
            if (query.ref) {
                eq('ref', query.ref)
            } else if (query.reference) {
                eq('ref', crmCoreService.getReferenceIdentifier(query.reference))
            }
            if (query.type) {
                eq('type', query.type)
            }
            if (query.subject) {
                ilike('subject', SearchUtils.wildcard(query.subject))
            }
            if (query.price) {
                if (query.price.startsWith('<')) {
                    lt('price', Integer.valueOf(query.price.substring(1)))
                } else if (query.price.startsWith('>')) {
                    gt('price', Integer.valueOf(query.price.substring(1)))
                } else if (query.price.contains('-')) {
                    def range = query.price.split('-').toList()
                    between('price', Integer.valueOf(range[0]), Integer.valueOf(range[1]))
                } else {
                    eq('price', Integer.valueOf(query.price))
                }
            }
        }
        return result
    }

    CrmClassified get(Long id) {
        CrmClassified.findByIdAndTenantId(id, TenantUtils.tenant)
    }

    List<String> listClassifiedTypes() {
        grailsApplication.config.crm.classified.types ?:
                ["Köpes", "Säljes", "Önskas hyra", "Uthyres", "Sökes", "Finnes", "Övrigt"]
    }
}
