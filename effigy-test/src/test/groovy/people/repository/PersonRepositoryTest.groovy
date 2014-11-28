package people.repository

import com.stehno.effigy.jdbc.EffigyEntityRowMapper
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.springframework.test.jdbc.JdbcTestUtils
import people.DatabaseEnvironment
import people.entity.Person

class PersonRepositoryTest {

    @Rule public DatabaseEnvironment database = new DatabaseEnvironment()

    private PersonRepository personRepository

    @Before void before() {
        personRepository = new EffigyPersonRepository(
            jdbcTemplate: database.jdbcTemplate
        )
    }

    @Test void create() {
        Person personA = new Person(
            firstName: 'John',
            middleName: 'Q',
            lastName: 'Public',
            birthDate: Date.parse('MM/dd/yyyy', '05/28/1970'),
            married: false
        )

        def id = personRepository.create(personA)

        assert id == 1
        assert JdbcTestUtils.countRowsInTable(database.jdbcTemplate, 'people') == 1

        def result = personRepository.retrieve(1)
        assert result == personA

        Person personB = new Person(
            firstName: 'Abe',
            middleName: 'A',
            lastName: 'Ableman',
            birthDate: Date.parse('MM/dd/yyyy', '05/28/1970'),
            married: true
        )

        def idB = personRepository.create(personB)

        assert JdbcTestUtils.countRowsInTable(database.jdbcTemplate, 'people') == 2

        def people = personRepository.retrieveAll()
        assert people.size() == 2

        people.each { p->
            println p
        }

        assert !personRepository.delete(100)

        assert personRepository.delete(1)
        assert JdbcTestUtils.countRowsInTable(database.jdbcTemplate, 'people') == 1

        assert personRepository.deleteAll()
        assert JdbcTestUtils.countRowsInTable(database.jdbcTemplate, 'people') == 0
    }

    @Test void update() {
        Person personA = new Person(
            firstName: 'John',
            middleName: 'Q',
            lastName: 'Public',
            birthDate: Date.parse('MM/dd/yyyy', '05/28/1970'),
            married: false
        )

        def id = personRepository.create(personA)

        assert personA == personRepository.retrieve(id)

        Person personB = new Person(
            id: id,
            firstName: 'Able',
            middleName: 'A',
            lastName: 'Ableman',
            birthDate: Date.parse('MM/dd/yyyy', '05/28/1970'),
            married: true
        )

        personRepository.update(personB)

        assert personB == personRepository.retrieve(id)
    }
}
