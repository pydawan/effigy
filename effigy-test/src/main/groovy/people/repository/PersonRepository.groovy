package people.repository

import com.stehno.effigy.annotation.Limited
import com.stehno.effigy.repository.CrudOperations
import people.entity.Person

/**
 * Created by cjstehno on 11/26/2014.
 */
interface PersonRepository extends CrudOperations<Person, Long> {

    List<Person> findByMarried(boolean married)

    @Limited(2)
    List<Person> findByFirstName(String firstName)
}