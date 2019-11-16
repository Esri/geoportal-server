using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Collections;
using System.Net;
using System.Security;
using System.Security.Cryptography.X509Certificates;

namespace com.esri.gpt.security
{
    public class ClientCertRequest
    {
        private static Hashtable certificates = new Hashtable();

        private static bool AcceptValidCertificates(object sender, X509Certificate certificate, X509Chain chain, System.Net.Security.SslPolicyErrors sslPolicyErrors)
        {
            // If the certificate is a valid, signed certificate, return true.
            if (sslPolicyErrors == System.Net.Security.SslPolicyErrors.None)
            {
                return true;
            }

            // If there are errors in the certificate chain, look at each error to determine the cause.
            if ((sslPolicyErrors & System.Net.Security.SslPolicyErrors.RemoteCertificateChainErrors) != 0)
            {
                if (chain != null && chain.ChainStatus != null)
                {
                    foreach (System.Security.Cryptography.X509Certificates.X509ChainStatus status in chain.ChainStatus)
                    {
                        if ((certificate.Subject == certificate.Issuer) &&
                           (status.Status == System.Security.Cryptography.X509Certificates.X509ChainStatusFlags.UntrustedRoot))
                        {
                            // Self-signed certificates with an untrusted root are valid. 
                            continue;
                        }
                        else
                        {
                            if (status.Status != System.Security.Cryptography.X509Certificates.X509ChainStatusFlags.NoError)
                            {
                                // If there are any other errors in the certificate chain, the certificate is invalid,
                                // so the method returns false.
                                return false;
                            }
                        }
                    }
                }

                // When processing reaches this line, the only errors in the certificate chain are 
                // untrusted root errors for self-signed certificates. These certificates are valid
                // for default Exchange server installations, so return true.
                return true;
            }
            else
            {
                // In all other cases, return false.
                return false;
            }
        }

        public static void handleClientCert(HttpWebRequest request, String url)
        {            
            if (url == null || url.Length == 0)
            {
                return;
            }
            //else if (!url.StartsWith("https://"))
            else
            {
                return;
            }
            
            X509Certificate2 cert = null;

            if(certificates.ContainsKey(url)){
                IDictionaryEnumerator denum = certificates.GetEnumerator();
                DictionaryEntry dentry;
                while (denum.MoveNext())
                {
                    dentry = (DictionaryEntry) denum.Current;
                    if ((String) dentry.Key == url)
                    {
                        cert = (X509Certificate2) dentry.Value ;
                        break;
                    }
                }                
            } else {

                // Established what should happen when validating Server certificates when establishing an SSL connection
                ServicePointManager.ServerCertificateValidationCallback = new System.Net.Security.RemoteCertificateValidationCallback(AcceptValidCertificates);
                
                // Create a connection to the current user's Certificate Repository in order to retrieve potential user certificates for passing
                X509Store store = new X509Store(StoreName.My, StoreLocation.CurrentUser);
                store.Open(OpenFlags.ReadOnly);
                X509Certificate2Collection col = store.Certificates.Find(X509FindType.FindByKeyUsage, X509KeyUsageFlags.DigitalSignature, true);
                X509Certificate2Collection sel = X509Certificate2UI.SelectFromCollection(col, "PKI User Certificates that support Digital Signatures", "Select your internal PKI Certificates", X509SelectionFlag.SingleSelection);
                if (sel.Count > 0)
                {
                    X509Certificate2Enumerator en = sel.GetEnumerator();
                    en.MoveNext();
                    cert = en.Current;
                    certificates.Add(url, cert);
                }
                store.Close();
            }

            // add certificate to list certificates in request
            request.ClientCertificates.Add(cert);
        }
    }
}
