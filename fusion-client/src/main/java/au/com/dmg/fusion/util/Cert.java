package au.com.dmg.fusion.util;


public class Cert {
  
    private static final String TestEnvironment = "MIIGEjCCA/qgAwIBAgIRAPeCIneztajhC2LD+k4K+RwwDQYJKoZIhvcNAQEMBQAw gYgxCzAJBgNVBAYTAlVTMRMwEQYDVQQIEwpOZXcgSmVyc2V5MRQwEgYDVQQHEwtK ZXJzZXkgQ2l0eTEeMBwGA1UEChMVVGhlIFVTRVJUUlVTVCBOZXR3b3JrMS4wLAYD VQQDEyVVU0VSVHJ1c3QgUlNBIENlcnRpZmljYXRpb24gQXV0aG9yaXR5MB4XDTE0 MDkxMDAwMDAwMFoXDTI0MDkwOTIzNTk1OVowgYYxCzAJBgNVBAYTAlVTMQswCQYD VQQIEwJERTETMBEGA1UEBxMKV2lsbWluZ3RvbjEkMCIGA1UEChMbQ29ycG9yYXRp b24gU2VydmljZSBDb21wYW55MS8wLQYDVQQDEyZUcnVzdGVkIFNlY3VyZSBDZXJ0 aWZpY2F0ZSBBdXRob3JpdHkgNTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoC ggEBAI0GNgsj2QI1JOdYk8aNg/0JtkcQDJ8oVyVm1qosht+fd7UJuxnE+cfbrEiV heNHkCTUSAgHNYPAtMvTRpTW5SoXp5ywE9vstT+QEyTCh9hXe/Ix+9rHKaYiRV+H vfJapC9UmXHFP0V/eQPMRcS6kFjY/kLgGkpy/NmBblvAfw+BIqW7u1l+lxkJ9qOu SzcetveuGLsuekM9cc0bzChx5W3lc0kAbX/1KKiaByk/oMf3qHFkDf9q2KfrpY9A /KE4hgLdTC5hKrQrehazl7b+Epmx8G2MvsK28Vl7m1QD35vxtKHHiuDNOQdF5Ct4 JtXfi2Kuzi1Q6bEVQymayy1DjwcCAwEAAaOCAXUwggFxMB8GA1UdIwQYMBaAFFN5 v1qqK0rPVIDh2JvAnfKyA2bLMB0GA1UdDgQWBBTyu1Xu/I/P0D8UaBqVfnkOqxcw 9DAOBgNVHQ8BAf8EBAMCAYYwEgYDVR0TAQH/BAgwBgEB/wIBADAdBgNVHSUEFjAU BggrBgEFBQcDAQYIKwYBBQUHAwIwIgYDVR0gBBswGTANBgsrBgEEAbIxAQICCDAI BgZngQwBAgIwUAYDVR0fBEkwRzBFoEOgQYY/aHR0cDovL2NybC51c2VydHJ1c3Qu Y29tL1VTRVJUcnVzdFJTQUNlcnRpZmljYXRpb25BdXRob3JpdHkuY3JsMHYGCCsG AQUFBwEBBGowaDA/BggrBgEFBQcwAoYzaHR0cDovL2NydC51c2VydHJ1c3QuY29t L1VTRVJUcnVzdFJTQUFkZFRydXN0Q0EuY3J0MCUGCCsGAQUFBzABhhlodHRwOi8v b2NzcC51c2VydHJ1c3QuY29tMA0GCSqGSIb3DQEBDAUAA4ICAQAGsUdhGf+feSte 4SOKj+2XtTfw4uo5t21lm1kXoRPM/ObB8yzzuVscvwnZ8Dn8PXjQXlP3ycqtc91X g4i63y48TUjsrK1/d6IvWAuzMN7tkEzVbBVFWlZz9jxYMmeGhrZ5HFOIjYJRRduQ 4jTYZFjX+cm5b8baZuS43nRqsYGARFYlsxIzUGSOITM6W0QZ7s15p6Nh7nRMGR/g m2qShUIzj2RDEz2XXDDTsVT9NnN7b2WhbBMmsXRxY7ERL/oZ6sZLzz7g0tdP/fOx geY+CWp891EqIxQLd5HYdIyGXesILMu8EaX9zMY76kbahJ0HKL//f0+S2SKDaYe7 6APSyu1jqjfEUeaBSlPlvP5pXbygHjr/gQDVPyFzre6+Di+qZSIvcWuqo/jV2jJk Ixd1rieFcsdkepYyAPC5GxNzHg0eWG9N669bnSxpvVDvmEl6ztbp7gxM3ciisBQz OLApig0V1N+0+YUXUq5f/0lenGZ9cqN3cs0/8ClTp1p3o84ErzFhjWQCIaBTODTS hYvB1+z6Hf2ljqD50KHs/80KO4mQBsPZjod8rQQa2KP0W3yvCBR6Z7ZUKTGGB0FV Q29vl2FmGkHV80dWIIgWzkU6ajnQXygkTr46jKxNXqT+G5+FaY79d0Vpf9XNg+m1 Kw/4P1yG/5xtH6HrU2uqz3qOmM4yWg==";

    private static final String ProductionEnvironment = "MIIDXzCCAkegAwIBAgILBAAAAAABIVhTCKIwDQYJKoZIhvcNAQELBQAwTDEgMB4GA1UECxMXR2xvYmFsU2lnbiBSb290IENBIC0gUjMxEzARBgNVBAoTCkdsb2JhbFNpZ24xEzARBgNVBAMTCkdsb2JhbFNpZ24wHhcNMDkwMzE4MTAwMDAwWhcNMjkwMzE4MTAwMDAwWjBMMSAwHgYDVQQLExdHbG9iYWxTaWduIFJvb3QgQ0EgLSBSMzETMBEGA1UEChMKR2xvYmFsU2lnbjETMBEGA1UEAxMKR2xvYmFsU2lnbjCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAMwldpB5BngiFvXAg7aEyiie/QV2EcWtiHL8RgJDx7KKnQRfJMsuS+FggkbhUqsMgUdwbN1k0ev1LKMPgj0MK66X17YUhhB5uzsTgHeMCOFJ0mpiLx9e+pZo34knlTifBtc+ycsmWQ1z3rDI6SYOgxXG71uL0gRgykmmKPZpO/bLyCiR5Z2KYVc3rHQU3HTgOu5yLy6c+9C7v/U9AOEGM+iCK65TpjoWc4zdQQ4gOsC0p6Hpsk+QLjJg6VfLuQSSaGjlOCZgdbKfd/+RFO+uIEn8rUAVSNECMWEZXriX7613t2Saer9fwRPvm2L7DWzgVGkWqQPabumDk3F2xmmFghcCAwEAAaNCMEAwDgYDVR0PAQH/BAQDAgEGMA8GA1UdEwEB/wQFMAMBAf8wHQYDVR0OBBYEFI/wS3+oLkUkrk1Q+mOai97i3Ru8MA0GCSqGSIb3DQEBCwUAA4IBAQBLQNvAUKr+yAzv95ZURUm7lgAJQayzE4aGKAczymvmdLm6AC2upArT9fHxD4q/c2dKg8dEe3jgr25sbwMpjjM5RcOO5LlXbKr8EpbsU8Yt5CRsuZRj+9xTaGdWPoO4zzUhw8lo/s7awlOqzJCK6fBdRoyV3XpYKBovHd7NADdBj+1EbddTKJd+82cEHhXXipa0095MJ6RMG3NzdvQXmcIfeg7jLQitChws/zyrVQ4PkX4268NXSb7hLi18YIvDQVETI53O9zJrlAGomecsMx86OyXShkDOOyyGeMlhLxS67ttVb9+E7gUJTb0o2HLO02JQZR7rkpeDMdmztcpHWD9f";

	public static String getCertificate(boolean isTestEnvironment) {
        String beg = "-----BEGIN CERTIFICATE-----\n";
        String end = "\n-----END CERTIFICATE-----";
        if(isTestEnvironment){
            return beg + TestEnvironment + end;
        }
        else {
            return beg + ProductionEnvironment + end;
        }
    } 

}
