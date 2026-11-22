# Class demo और viva guide

## 5–7 मिनट का प्रभावी demo

**शुरुआत — 30 सेकंड**

“मेरा project Structura — Data Structure Visualizer है। यह Java Swing में बनाया गया offline desktop application है। इसमें Linked List, Stack, Queue, BST और AVL को operation के हर meaningful step पर देखा जा सकता है। उद्देश्य केवल result दिखाना नहीं, बल्कि pointers और tree structure में हुए बदलाव समझाना है।”

**Linked List — 45 सेकंड**

Linked List चुनें। Insert में value 10/index 0, फिर 20/index 1, फिर 15/index 1 डालें। हर operation पूरी करें या Go to end दबाएँ। Traverse चलाएँ: output `[10, 15, 20]` होगा। Index 1 delete करके समझाएँ कि predecessor का `next` अब अगले node को refer करता है। Index zero-based होता है।

**Stack बनाम Queue — 45 सेकंड**

Stack में 10 और 20 push करें, फिर pop: 20 निकलेगा। Queue में 10 और 20 enqueue करें, फिर dequeue: 10 निकलेगा। कहें: “Stack LIFO है, Queue FIFO है। दोनों linked nodes से implement किए गए हैं और access operations O(1) हैं।”

**BST बनाम AVL — 90 सेकंड**

BST → Sorted input → Load example → Go to end: सात nodes की height 7 दिखाएँ। AVL पर वही example चलाएँ: height 3 होगी। यह तुलना दो demonstrations के माध्यम से है; app में simultaneous side-by-side comparison नहीं है। कहें: “BST में sorted input एक chain बना सकता है। AVL rotations के द्वारा height नियंत्रित रखता है।”

**सबसे अच्छा demo: LR rotation — 60 सेकंड**

AVL में LR rotation example load करें, Pause दबाएँ और Next से आगे बढ़ें। Sequence 30, 10, 20 है। `LR ROTATION` step में root 30 का balance +2 होगा। पहले 10 पर left rotation, फिर 30 पर right rotation दिखेगी। अंतिम root 20 और children 10, 30 होंगे। Replay से इसे फिर दिखा सकते हैं।

**Correctness और design — 45 सेकंड**

AVL/BST में पहले से मौजूद value डालकर duplicate validation दिखाएँ। Empty Stack पर Pop और value में `abc` डालकर साफ़ error दिखाएँ। बताएं: “Invalid request state नहीं बदलती। Models और UI अलग हैं। Immutable snapshots पुराने animation frames सुरक्षित रखते हैं।”

**समापन — 30 सेकंड**

Export PNG से एक diagram save करें। Test record खोलकर 12 passing JUnit tests और randomized checks दिखाएँ। कहें: “मेरे project में visualization, algorithm explanation और automated correctness checks तीनों हैं।” Test assertion count को अलग-अलग test cases की संख्या बताकर प्रस्तुत न करें।

## जरूरी viva प्रश्न

| प्रश्न | संक्षिप्त उत्तर |
|---|---|
| Swing क्यों? | PDF में Java desktop scope है। Swing JDK में शामिल है और external runtime library की जरूरत नहीं है। |
| Linked List में array इस्तेमाल किया? | Live structure explicit ListNode references से बना है। Lists snapshots और traversal output के लिए हैं। |
| AVL, BST से कैसे अलग है? | BST ordering के साथ प्रत्येक node पर left/right subtree height का difference अधिकतम 1 रखता है। |
| Balance factor क्या है? | height(left) − height(right)। Empty tree height 0 और leaf height 1। |
| LL और LR में अंतर? | LL में single right rotation; LR में left-child पर left rotation, फिर parent पर right rotation। |
| Two-child deletion कैसे? | Right subtree का minimum यानी inorder successor चुनते हैं। Key replace करके successor detach करते हैं। |
| Duplicate values? | Linear structures में allowed; trees में duplicate keys rejected। IDs values से अलग हैं। |
| Immutable snapshot क्यों? | Mutable node references रखने से पुराने frames भी बदल जाते। Snapshot में copied primitive data है। |
| Previous step undo है? | नहीं। यह visual replay है। Model operation पहले पूरा हो चुका है। |
| UI freeze क्यों नहीं होती? | Playback Swing Timer पर है; UI thread पर sleep नहीं है। Operations 30 nodes तक bounded हैं। |
| Big-O और animation speed का संबंध? | Delay presentation के लिए है। Big-O core algorithm का है; snapshots/rendering अतिरिक्त overhead हैं। |
| AVL deletion कठिन क्यों? | Removal से कई ancestors की height बदल सकती है, इसलिए कई जगह rebalance करना पड़ सकता है। |
| Testing कैसे हुई? | Deterministic random operations को ArrayList/ArrayDeque/TreeSet से compare किया; ordering, size, heights और balance check किए। |
| OOP कहाँ इस्तेमाल है? | Model interface, abstract base classes, BST से AVL inheritance, overridden methods और private/internal node state। |

## Presentation से पहले

1. उसी laptop पर `Run.cmd` खोलकर एक rehearsal करें।
2. Java 17+ जाँचें। मौजूदा development machine पर Java 21 उपलब्ध है।
3. Project ZIP extract करने के बाद app चलाएँ; ZIP के भीतर से नहीं।
4. Projector पर window maximize करें। जरूरत हो तो canvas में Fit / zoom उपयोग करें।
5. `Export PNG` से image save कर देख लें; file chooser का वास्तविक desktop rehearsal उपयोगी है।
6. `docs/screenshots` और test record backup रखें। इन्हें live-demo का विकल्प बताकर ही दिखाएँ।
7. Code में `AVLModel`, `StepSnapshot`, `AnimationPlayer` और `Verification` समझकर जाएँ।

सबसे मजबूत प्रभाव सही explanation, stable demo और अपने implementation की समझ से आएगा। किसी class rank की गारंटी नहीं दी जा सकती।
