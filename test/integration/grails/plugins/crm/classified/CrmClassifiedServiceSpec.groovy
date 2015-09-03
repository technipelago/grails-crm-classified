package grails.plugins.crm.classified

/**
 * Created by goran on 15-09-02.
 */
class CrmClassifiedServiceSpec extends grails.test.spock.IntegrationSpec {

    def crmClassifiedService

    def "create classified"() {
        given:
        def types = crmClassifiedService.listClassifiedTypes()

        when:
        def instance = crmClassifiedService.createClassified(
                location: "Internet",
                type: types[1],
                subject: "Bits for sale",
                message: "I have a few bits left on my Google Drive.",
                address: "42 Test Street, Surftown",
                true
        )

        then:
        instance.hasErrors()

        when:
        instance.telephone = "555 5555 55"

        then:
        instance.validate()
        instance.save(failOnError: true)
    }
}
