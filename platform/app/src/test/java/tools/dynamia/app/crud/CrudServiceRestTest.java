package tools.dynamia.app.crud;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tools.dynamia.domain.services.CrudService;

@SpringBootTest(classes = CrudTestConfiguration.class)
public class CrudServiceRestTest {

    @Autowired
    private CrudService crudService;


    @Test
    public void createEntity() {
        crudService.findAll(PersonEntity.class);
    }


}
