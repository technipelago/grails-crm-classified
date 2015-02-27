package grails.plugins.crm.classified

import grails.plugins.crm.core.AuditEntity
import grails.plugins.crm.core.TenantEntity

@AuditEntity
@TenantEntity
class CrmClassified {

    public static final int STATUS_DRAFT = 0
    public static final int STATUS_REVIEW = 1
    public static final int STATUS_REJECT = -1
    public static final int STATUS_PUBLISH = 2
    public static final int STATUS_ARCHIVE = 9

    int status
    String ref
    String location
    String type
    String subject
    String message
    String address
    String email
    String telephone
    String url
    Integer price

    static constraints = {
        status(inList: [STATUS_DRAFT, STATUS_REVIEW, STATUS_REJECT, STATUS_PUBLISH, STATUS_ARCHIVE])
        ref(maxSize: 80, nullable: true)
        location(maxSize: 40, nullable: true)
        type(maxSize: 40, blank: false)
        subject(maxSize: 100, blank: false)
        message(maxSize: 2000, blank: false)
        address(maxSize: 80, nullable: true)
        email(maxSize: 80, nullable: true, email: true, validator: { val, obj ->
            val || obj.telephone ? null : 'blank'
        })
        telephone(maxSize: 20, nullable: true, validator: { val, obj ->
            val || obj.email ? null : 'blank'
        })
        url(maxSize: 255, nullable: true)
        price(min: 0, nullable: true)
    }

    static transients = ['draft', 'review', 'reject', 'publish', 'archive', 'statusName']

    static taggable = true

    static searchable = {
        only = ['type', 'subject', 'message', 'address']
        subject boost: 20
    }

    public static final List<String> BIND_WHITELIST = [
            'status',
            'location',
            'type',
            'subject',
            'message',
            'address',
            'email',
            'telephone',
            'url',
            'price'
    ]

    transient boolean isDraft() {
        status == STATUS_DRAFT
    }

    transient boolean isReview() {
        status == STATUS_REVIEW
    }

    transient boolean isReject() {
        status == STATUS_REJECT
    }

    transient boolean isPublish() {
        status == STATUS_PUBLISH
    }

    transient boolean isArchive() {
        status == STATUS_ARCHIVE
    }

    transient String getStatusName() {
        switch (status) {
            case STATUS_DRAFT:
                return 'draft'
            case STATUS_REVIEW:
                return 'review'
            case STATUS_REJECT:
                return 'reject'
            case STATUS_PUBLISH:
                return 'publish'
            case STATUS_ARCHIVE:
                return 'archive'
        }
    }

    String toString() {
        subject.toString()
    }
}
