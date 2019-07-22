
## Terminology

| Term         | Definition  |
|--------------|-------------|
| Enki         | The core Enki service. Enki Core collects consent data relating to PII Data. Enki is a data processor as defined in the GDPR. It does NOT store PII information, only metadata (what is stored where and shared with whom for how long). |
| Consent Data | Data relating to an individual data subject’s (referenced by their Enki ID) agreement for the time bounded use of PII in relation to a specific activity.  |
| Consus       | A cryptographically secure PII store.
| PII          | Personally Identifiable Information: anything that can be used to identify an individual.  |
| PII Data     | Refers to the piece of data that embodies the PII, such as the data subject’s name, passport copy, or a credit reference  |
| Bletchley    | A cryptography and message serialisation framework written with utmost security as top priority.  |
| Enki Agent   | Agent running at a partner organisation’s side to coordinate and validate the storage/retrieval of data.  |
| Metadata     | Data about a piece of data. e.g. when it was created. |
| Data Subject     | A natural person whose personal data is processed by a Controller or Processor. |
| Data Controller  | The entity that determines the purposes, conditions and means of the processing of personal data. |
| Data Processor   | The entity that processes data on behalf of the Data Controller. |

## User Journey

### The following steps describe the user journey of how Enki can be used to transfer a user’s (Alice) already validated PII data from one bank (BankA) to a second bank (BankB) using Enki.

1. Alice is a client of BankA. She logs in to the BankA website and looks at her profile.

![](screenshots/1/2018-01-09T09-07-27.371316-BankA.png)

![](screenshots/1/2018-01-09T09-07-28.528694-BankA.png)

![](screenshots/1/2018-01-09T09-07-28.857083-BankA.png)

![](screenshots/1/2018-01-09T09-07-29.486951-BankA.png)

2. Alice returns to BankA. She goes to the profile view. She sees a prompt about connecting this account with Enki and she clicks "Connect".

![](screenshots/2/2018-01-09T09-07-27.371316-BankA.png)

![](screenshots/2/2018-01-09T09-07-30.134369-BankA.png)

![](screenshots/2/2018-01-09T09-07-31.592306-BankA.png)

![](presentation/2.png)

3. Alice goes to Enki and signs up for an account.

![](screenshots/4/2018-01-09T09-07-32.251572-Enki(main.hbs).png)

![](screenshots/4/2018-01-09T09-07-34.001970-Enki(main.hbs).png)

![](screenshots/4/2018-01-09T09-07-33.292254-Register.png)

4. In the process of signing up with Enki, a username and password is stored.

![](screenshots/5/2018-01-09T09-07-35.822992-Signintocontinue.png)

![](screenshots/5/2018-01-09T09-07-36.461407-Enki(main.hbs).png)

5. Alice opts to link Enki account with BankA.

![](screenshots/6/2018-01-09T09-07-36.751476-Enki(main.hbs).png)

6. Alice logs into BankA.

![](screenshots/7/2018-01-09T09-07-38.290618-BankA.png)
![](screenshots/7/2018-01-09T09-07-38.851583-BankA.png)

7. Alice grants Enki permissions to access the "open id" scope and she is redirected to Enki home page.

![](screenshots/8/2018-01-09T09-07-39.232158-BankA.png)
![](screenshots/8/2018-01-09T09-07-39.856960-BankA.png)
![](screenshots/9/2018-01-09T09-07-40.817471-Enki(main.hbs).png)
![](presentation/5.png)

8. Alice goes to the Enki Dashboard and sees PII metadata entered during BankA signup in Enki.

![](screenshots/10/2018-01-09T09-07-41.431349-Enki(main.hbs).png)
![](screenshots/10/2018-01-09T09-07-41.740069-Enki(main.hbs).png)

9. Alice goes to BankB and uses the 'Existing Enki account' button to sign up.

![](screenshots/11/2018-01-09T09-07-43.140255-BankB.png)
![](screenshots/11/2018-01-09T09-07-43.464838-BankB.png)
![](presentation/9.png)

10. Alice is forwarded to Enki and is asked which data BankB is allowed to use.

![](screenshots/12/2018-01-09T09-07-44.371022-.png)
![](screenshots/12/2018-01-09T09-07-45.869825-.png)
![](presentation/9_1-4.png)

11. Alice is returned to BankB which now has received BankA’s data.

![](screenshots/13/2018-01-09T09-07-47.682688-BankB.png)
![](presentation/9_5-7.png)

12. Alice completes the BankB registration process filling in the missing pieces.

![](screenshots/14/2018-01-09T09-07-48.804728-BankB.png)
![](screenshots/14/2018-01-09T09-07-50.730167-BankB.png)

![](screenshots/14/2018-01-09T09-07-54.139787-BankB.png)

![](screenshots/14/2018-01-09T09-07-49.129553-BankB.png)

![](screenshots/14/2018-01-09T09-07-51.376168-BankB.png)

![](screenshots/14/2018-01-09T09-07-54.459823-BankB.png)

![](screenshots/14/2018-01-09T09-07-49.790872-BankB.png)

![](screenshots/14/2018-01-09T09-07-51.696923-BankB.png)

![](screenshots/14/2018-01-09T09-07-55.699081-BankB.png)

![](screenshots/14/2018-01-09T09-07-50.116393-BankB.png)

![](screenshots/14/2018-01-09T09-07-52.359340-BankB.png)

![](screenshots/14/2018-01-09T09-07-56.520690-BankB.png)

13. Alice goes to Enki and links account with BankB.

![](screenshots/16/2018-01-09T09-07-57.252448-Enki(main.hbs).png)

![](screenshots/16/2018-01-09T09-08-00.985987-BankB.png)

![](screenshots/16/2018-01-09T09-07-58.080677-Signintocontinue.png)

![](screenshots/16/2018-01-09T09-08-01.890666-BankB.png)

![](screenshots/16/2018-01-09T09-07-58.707475-Enki(main.hbs).png)

![](screenshots/16/2018-01-09T09-08-02.724168-Enki(main.hbs).png)

14. Alice now goes to Enki and can see assertions from both BankA and BankB

![](screenshots/17/2018-01-09T09-08-03.013001-Enki(main.hbs).png)
![](screenshots/17/2018-01-09T09-08-03.662170-Enki(main.hbs).png)
![](presentation/15.png)
