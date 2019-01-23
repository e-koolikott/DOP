package ee.hm.dop.service.synchronizer.oaipmh;

import ee.hm.dop.model.Repository;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Method;

import static org.easymock.EasyMock.createMockBuilder;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@Ignore
@RunWith(EasyMockRunner.class)
public class RepositoryManagerTest {

    @Mock
    private MaterialIterator materialIterator;

    @Mock
    private MaterialParser materialParser;

    @Test
    public void getMaterialsFromWaramu() throws Exception {
        RepositoryManager repositoryManager = getRepositoryManager();
        Repository repository = getRepository();

        expect(repositoryManager.getMaterialIterator()).andReturn(materialIterator);

        //todo
        // expect(repositoryManager.getMaterialParser()).andReturn(materialParser);

        materialIterator.setParser(materialParser);
        expectLastCall();

        expect(materialIterator.connect(repository)).andReturn(materialIterator);

        replay(repositoryManager, materialIterator, materialParser);

        MaterialIterator returnedIterator = repositoryManager.getMaterialsFrom(repository);

        verify(repositoryManager, materialIterator, materialParser);

        assertEquals(materialIterator, returnedIterator);
    }

    @Test
    public void getMaterialsWrongSchema() throws NoSuchMethodException {
        Repository repository = getRepository();
        String errorMessage = "No parser for schema randomSchema or wrong repository URL";
        RepositoryManager repositoryManager = getRepositoryManager();
        repository.setSchema("randomSchema");

        expect(repositoryManager.getMaterialIterator()).andReturn(materialIterator);

        replay(repositoryManager, materialIterator);

        try {
            repositoryManager.getMaterialsFrom(repository);
            fail("Exception expected.");
        } catch (Exception e) {
            assertEquals(errorMessage, e.getMessage());
        }

        verify(repositoryManager, materialIterator);
    }

    @Test
    public void getMaterialsNullSchema() throws NoSuchMethodException {
        Repository repository = getRepository();
        repository.setSchema(null);
        RepositoryManager repositoryManager = getRepositoryManager();

        expect(repositoryManager.getMaterialIterator()).andReturn(materialIterator);

        replay(repositoryManager, materialIterator);

        try {
            repositoryManager.getMaterialsFrom(repository);
            fail("Exception expected.");
        } catch (Exception e) {
            assertEquals(null, e.getMessage());
        }

        verify(repositoryManager, materialIterator);
    }

    private RepositoryManager getRepositoryManager() throws NoSuchMethodException {

        Method getMaterialIterator = RepositoryManager.class.getDeclaredMethod("getMaterialIterator");

        Method getMaterialParser = RepositoryManager.class.getDeclaredMethod("getWaramuMaterialParser");

        return createMockBuilder(RepositoryManager.class).addMockedMethods(getMaterialParser, getMaterialIterator)
                .createMock();
    }

    private Repository getRepository() {
        Repository repository = new Repository();
        repository.setId((long) 1);
        repository.setBaseURL("http://waramu.url");
        repository.setSchema("waramu");
        return repository;
    }
}
