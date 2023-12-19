import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.IHttpRequest;
import ca.uhn.fhir.rest.client.api.IHttpResponse;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class SampleClient
{

    static long averageTime = 0;
    public static void main(String[] theArgs) throws FileNotFoundException
    {

        // Create a FHIR client

        FhirContext fhirContext = FhirContext.forR4();
        IGenericClient client = fhirContext.newRestfulGenericClient("http://hapi.fhir.org/baseR4");
        client.registerInterceptor(new LoggingInterceptor(false));
        client.registerInterceptor(new IClientInterceptor() {

            @Override  public void interceptRequest(IHttpRequest iHttpRequest) {
                //Uncomment the following line to disable cache
                //iHttpRequest.addHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            }

            @Override
            public void interceptResponse(IHttpResponse iHttpResponse) throws IOException
            {
                System.out.println("Response time: " + iHttpResponse.getRequestStopWatch().toString());
                averageTime += iHttpResponse.getRequestStopWatch().getMillis();
            }
        });

        // Search for Patient resources
        Scanner scanner = new Scanner(new File("lastNames.txt"));
        scanner.useDelimiter("&&");

        while (scanner.hasNext())
        {
            String name = scanner.next();
            searchByLastName(name, client);
            System.out.println("--------------------------------------------");
        }
        scanner.close();
        System.out.println("Total Time: " + averageTime + "\nAverage Time: " + averageTime / 20);
    }

    private static void searchByLastName(String lastName, IGenericClient client){
        Bundle response = client
                .search()
                .forResource("Patient")
                .where(Patient.FAMILY.matches().value(lastName))
                .returnBundle(Bundle.class)
                .execute();

        List<Bundle.BundleEntryComponent> list = response.getEntry();

        //Display the patient's info.
        list.forEach(pat ->
        {
            Patient p = (Patient) pat.getResource();
            System.out.println((p.getName().get(0).getGiven() +", "+ p.getName().get(0).getFamily() +", "+ p.getBirthDate()));
        });
    }
}
