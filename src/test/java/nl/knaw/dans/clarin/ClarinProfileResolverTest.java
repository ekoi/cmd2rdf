package nl.knaw.dans.clarin;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.verifyNew;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.File;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ClarinProfileResolver.class})
public class ClarinProfileResolverTest {
	@Test
    public void createDirectoryStructureWhenPathDoesntExist() throws Exception {
        final String directoryPath = "mocked path";
 
        File directoryMock = mock(File.class);
 
    // This is how you tell PowerMockito to mock construction of a new File.
        whenNew(File.class).withArguments(directoryPath).thenReturn(directoryMock);
 
    // Standard expectations
        when(directoryMock.exists()).thenReturn(false);
        when(directoryMock.mkdirs()).thenReturn(true);

 
        // Optionally verify that a new File was "created".
       // verifyNew(File.class).withArguments(directoryPath);
    }
}
