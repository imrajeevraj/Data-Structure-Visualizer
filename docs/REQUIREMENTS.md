# Requirements, readiness and useful next improvements

## अभी project चलाने के लिए

| जरूरत | स्थिति / अर्थ |
|---|---|
| Java 17 या नया runtime | जरूरी; development machine पर Java 21.0.10 मिला |
| JDK 17+ | Source compile करने के लिए; Java compiler उपलब्ध था |
| Internet | Application चलाने के लिए नहीं; Maven/JUnit का पहला download हो तो जरूरी |
| Maven | Optional; offline build/test scripts शामिल हैं |
| Display | 1280×720 target; 1440×900 में अधिक content एक साथ दिखता है |
| RAM | PDF का 4 GB target, IDE के साथ 8 GB उपयोगी; hardware minimum benchmark नहीं किया गया |
| Backend / paid API / database | इस version में जरूरत नहीं |

## Submission पूरा करने के लिए आपसे उपयोगी जानकारी

App इन बातों के बिना भी तैयार और runnable है। College deliverables को अंतिम रूप देने के लिए इनकी जरूरत पड़ सकती है:

- Teacher का final-report format, page limit और marking rubric।
- Submission deadline और presentation की अवधि।
- क्या signed certificate/declaration, PPT, demo video या printed report अनिवार्य है?
- Final title, student details और guide details की पुष्टि। App में दिए गए नाम/ID source PDF से लिए गए हैं।
- Presentation laptop का OS, Java version और display scaling; उस पर rehearsal कर लें।

## कौन से enhancements पहले जोड़ना उपयोगी होगा?

ये सुझाव हैं; नीचे के features अभी implemented नहीं हैं।

| प्राथमिकता | सुधार | लाभ और वास्तविक अतिरिक्त काम |
|---|---|---|
| 1 | BST/AVL side-by-side comparison | समान input पर दोनों shapes और heights साथ दिखें; independent synchronized traces चाहिए |
| 2 | Highlighted Java pseudocode | हर visual step को algorithm line से जोड़ना; model phases और code lines की mapping चाहिए |
| 3 | Save/load sessions | Classroom examples अगली बार खोलना; versioned file format और malformed-file validation चाहिए |
| 4 | Hindi/English UI switch | Local learners के लिए उपयोगी; translated resource bundles और font/layout QA चाहिए |
| 5 | Signed/native installer with bundled runtime | Java setup सरल; target-OS packaging, runtime distribution terms और signing व्यवस्था चाहिए |

नए structures जोड़ने से पहले वर्तमान पाँचों का अच्छा explanation, test evidence और final-report consistency अधिक उपयोगी है। Graphs/heaps/sorting PDF के पहले version के scope में नहीं थे।

## वर्तमान सीमाएँ

- 30 live nodes, signed 32-bit integer input, एक active structure।
- कोई session persistence, cloud sync, collaboration या operation undo नहीं।
- Displayed Big-O theoretical है; measured performance comparison नहीं है।
- Automated checks Windows/Java 21 पर हुए; Java 17-compatible bytecode बनाया गया। Linux/macOS launch scripts दिए हैं, लेकिन वहाँ run नहीं किया गया।
- Swing component rendering/action checks हुए। Native export dialog, interactive quiz clicks और classroom projector behavior का manual rehearsal अभी करना चाहिए।
