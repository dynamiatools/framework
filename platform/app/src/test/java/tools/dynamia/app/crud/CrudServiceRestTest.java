package tools.dynamia.app.crud;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import tools.dynamia.domain.services.CrudService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CrudTestConfiguration.class)
public class CrudServiceRestTest {

    @Autowired
    private CrudService crudService;


    @Test
    public void createEntity() {
        crudService.findAll(PersonEntity.class);
    }


}
