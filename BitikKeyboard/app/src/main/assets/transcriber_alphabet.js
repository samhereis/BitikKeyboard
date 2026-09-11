 (function(global){
     // --- Enum (typos preserved) ---
     const CharacterType = {
         epmty: "epmty",
         Consonant: "Consonant",
         Consonant_Univ: "Consonant_Univ",
         HardVowel: "HardVowel",
         SoftVowel: "SoftVowel",
         Special: "Special",
         Special_Hard_ConsonantFirst: "Special_Hard_ConsonantFirst",
         Special_Hard_VowelFirst: "Special_Hard_VowelFirst",
         Special_Soft_ConsonantFirst: "Special_Soft_ConsonantFirst",
         Special_Soft_VowelFirst: "Special_Soft_VowelFirst",
     };
     
     // --- Data classes ---
     class TranscriptionEntry {
         constructor(symbol, transcription_hard, transcription_soft, type){
             this.symbol = symbol;
             this.transcription_hard = transcription_hard;
             this.transcription_soft = transcription_soft;
             this.type = type;
         }
     }
     
     class TranscriptoinUnit {
         constructor(){
             this.self = null;
             this.nextVowel = null;
             this.prevVowel = null;
             this.isSoft = false;
             this.result = "";
         }
         
         SetNextVowel(prevVowel){
             // same parameter name and assignment as C#
             this.nextVowel = prevVowel;
         }
         
         SetPreviousVowel(prevVowel){
             // BUG PRESERVED FROM C#: assigns nextVowel instead of prevVowel
             this.nextVowel = prevVowel;
         }
         
         Validate(){
             if (this.self == null){
                 return;
             }
             
             if (this.self.type === CharacterType.Consonant || this.self.type === CharacterType.Consonant_Univ){
                 if (this.nextVowel != null){
                     if (this.nextVowel.type === CharacterType.HardVowel){
                         this.result = this.self.transcription_hard;
                     } else {
                         this.result = this.self.transcription_soft;
                         this.isSoft = true;
                     }
                 } else if (this.prevVowel != null){
                     if (this.prevVowel.type === CharacterType.HardVowel){
                         this.result = this.self.transcription_hard;
                     } else {
                         this.result = this.self.transcription_soft;
                         this.isSoft = true;
                     }
                 } else {
                     this.result = this.self.transcription_hard;
                 }
             } else {
                 this.result = this.self.transcription_hard;
             }
         }
     }
     
     class Transcrptiber_Old {
         constructor(){
             this._transcriptionList = [
                 new TranscriptionEntry("й", "𐰖", "𐰘", CharacterType.Consonant),
                 new TranscriptionEntry("ң", "𐰬", "𐰭", CharacterType.Consonant),
                 new TranscriptionEntry("ҥ", "𐰬", "𐰭", CharacterType.Consonant),
                 new TranscriptionEntry("у", "𐰆", "𐰆", CharacterType.HardVowel),
                 new TranscriptionEntry("ұ", "𐰆", "𐰆", CharacterType.HardVowel),
                 new TranscriptionEntry("к", "𐰴", "𐰚", CharacterType.Consonant),
                 new TranscriptionEntry("е", "𐰅", "𐰅", CharacterType.SoftVowel),
                 new TranscriptionEntry("н", "𐰣", "𐰤", CharacterType.Consonant),
                 new TranscriptionEntry("г", "𐰍", "𐰏", CharacterType.Consonant),
                 new TranscriptionEntry("ш", "𐱀", "𐱁", CharacterType.Consonant),
                 new TranscriptionEntry("ү", "𐰈", "𐰈", CharacterType.SoftVowel),
                 new TranscriptionEntry("ӱ", "𐰈", "𐰈", CharacterType.SoftVowel),
                 new TranscriptionEntry("з", "𐰕", "𐰕", CharacterType.Consonant_Univ),
                 new TranscriptionEntry("х", "𐰴", "𐰚", CharacterType.Consonant),
                 new TranscriptionEntry("ы", "𐰃", "𐰃", CharacterType.HardVowel),
                 new TranscriptionEntry("в", "𐰉", "𐰌", CharacterType.Consonant),
                 new TranscriptionEntry("а", "𐰀", "𐰀", CharacterType.HardVowel),
                 new TranscriptionEntry("п", "𐰯", "𐰯", CharacterType.Consonant_Univ),
                 new TranscriptionEntry("р", "𐰺", "𐰼", CharacterType.Consonant),
                 new TranscriptionEntry("о", "𐰆", "𐰆", CharacterType.HardVowel),
                 new TranscriptionEntry("л", "𐰞", "𐰠", CharacterType.Consonant),
                 new TranscriptionEntry("д", "𐰑", "𐰓", CharacterType.Consonant),
                 new TranscriptionEntry("ж", "𐰳", "𐰙", CharacterType.Consonant),
                 new TranscriptionEntry("э", "𐰅", "𐰅", CharacterType.SoftVowel),
                 new TranscriptionEntry("ч", "𐰲", "𐰲", CharacterType.Consonant_Univ),
                 new TranscriptionEntry("с", "𐰽", "𐰾", CharacterType.Consonant),
                 new TranscriptionEntry("м", "𐰢", "𐰢", CharacterType.Consonant_Univ),
                 new TranscriptionEntry("и", "𐰄", "𐰄", CharacterType.SoftVowel),
                 new TranscriptionEntry("т", "𐱄", "𐱅", CharacterType.Consonant),
                 new TranscriptionEntry("ө", "𐰇", "𐰇", CharacterType.SoftVowel),
                 new TranscriptionEntry("б", "𐰉", "𐰌", CharacterType.Consonant),
                 new TranscriptionEntry("ə", "𐰅", "𐰅", CharacterType.SoftVowel),
                 new TranscriptionEntry("қ", "𐰴", "𐰚", CharacterType.Consonant),
                 new TranscriptionEntry("ғ", "𐰍", "𐰏", CharacterType.Consonant),
                 new TranscriptionEntry("ф", "𐰯", "𐰯", CharacterType.Consonant_Univ),
                 new TranscriptionEntry("в", "𐰉", "𐰌", CharacterType.Consonant),
                 new TranscriptionEntry("х", "𐰴", "𐰚", CharacterType.Consonant),
                 new TranscriptionEntry("ц", "𐰽", "𐰾", CharacterType.Consonant),
                 new TranscriptionEntry("щ", "𐱀", "𐱁", CharacterType.Consonant),
                 new TranscriptionEntry("нт", "𐰦", "𐰦", CharacterType.Special),
                 new TranscriptionEntry("рт", "𐱈", "𐱈", CharacterType.Special),
                 new TranscriptionEntry("лт", "𐰡", "𐰡", CharacterType.Special),
                 new TranscriptionEntry("нч", "𐰨", "𐰨", CharacterType.Special),
                 new TranscriptionEntry("кы", "𐰶", "𐰶", CharacterType.Special_Hard),
                 new TranscriptionEntry("ык", "𐰷", "𐰷", CharacterType.Special_Hard),
                 new TranscriptionEntry("от", "𐱇", "𐱇", CharacterType.Special_Hard),
                 new TranscriptionEntry("ок", "𐰹", "𐰹", CharacterType.Special_Hard),
                 new TranscriptionEntry("ук", "𐰸", "𐰸", CharacterType.Special_Hard),
                 new TranscriptionEntry("өк", "𐰝", "𐰝", CharacterType.Special_Soft),
                 new TranscriptionEntry("үк", "𐰰", "𐰰", CharacterType.Special_Soft),
                 new TranscriptionEntry("ь", "𐰄", "𐰄", CharacterType.SoftVowel),
                 new TranscriptionEntry("ъ", "𐰃", "𐰃", CharacterType.HardVowel),
                 
                 new TranscriptionEntry("q", "𐰴", "𐰚", CharacterType.Consonant),
                 new TranscriptionEntry("ş", "𐱀", "𐱁", CharacterType.Consonant),
                 new TranscriptionEntry("e", "𐰅", "𐰅", CharacterType.SoftVowel),
                 new TranscriptionEntry("r", "𐰺", "𐰼", CharacterType.Consonant),
                 new TranscriptionEntry("t", "𐱄", "𐱅", CharacterType.Consonant),
                 new TranscriptionEntry("y", "𐰖", "𐰘", CharacterType.Consonant),
                 new TranscriptionEntry("u", "𐰆", "𐰆", CharacterType.HardVowel),
                 new TranscriptionEntry("ū", "𐰆", "𐰆", CharacterType.HardVowel),
                 new TranscriptionEntry("i", "𐰄", "𐰄", CharacterType.SoftVowel),
                 new TranscriptionEntry("i", "𐰄", "𐰄", CharacterType.SoftVowel),
                 new TranscriptionEntry("o", "𐰆", "𐰆", CharacterType.HardVowel),
                 new TranscriptionEntry("p", "𐰯", "𐰯", CharacterType.Consonant_Univ),
                 new TranscriptionEntry("a", "𐰀", "𐰀", CharacterType.HardVowel),
                 new TranscriptionEntry("ä", "𐰅", "𐰅", CharacterType.SoftVowel),
                 new TranscriptionEntry("s", "𐰽", "𐰾", CharacterType.Consonant),
                 new TranscriptionEntry("d", "𐰑", "𐰓", CharacterType.Consonant),
                 new TranscriptionEntry("g", "𐰍", "𐰏", CharacterType.Consonant),
                 new TranscriptionEntry("ü", "𐰈", "𐰈", CharacterType.SoftVowel),
                 new TranscriptionEntry("j", "𐰳", "𐰙", CharacterType.Consonant),
                 new TranscriptionEntry("ö", "𐰇", "𐰇", CharacterType.SoftVowel),
                 new TranscriptionEntry("k", "𐰴", "𐰚", CharacterType.Consonant),
                 new TranscriptionEntry("l", "𐰞", "𐰠", CharacterType.Consonant),
                 new TranscriptionEntry("ğ", "𐰍", "𐰏", CharacterType.Consonant),
                 new TranscriptionEntry("z", "𐰕", "𐰕", CharacterType.Consonant_Univ),
                 new TranscriptionEntry("ñ", "𐰬", "𐰭", CharacterType.Consonant),
                 new TranscriptionEntry("ŋ", "𐰬", "𐰭", CharacterType.Consonant),
                 new TranscriptionEntry("ç", "𐰲", "𐰲", CharacterType.Consonant_Univ),
                 new TranscriptionEntry("ı", "𐰃", "𐰃", CharacterType.HardVowel),
                 new TranscriptionEntry("b", "𐰉", "𐰌", CharacterType.Consonant),
                 new TranscriptionEntry("n", "𐰣", "𐰤", CharacterType.Consonant),
                 new TranscriptionEntry("m", "𐰢", "𐰢", CharacterType.Consonant_Univ),
                 new TranscriptionEntry("w", "𐰆", "𐰈", CharacterType.Consonant),
                 new TranscriptionEntry("f", "𐰯", "𐰯", CharacterType.Consonant_Univ),
                 new TranscriptionEntry("h", "𐰴", "𐰚", CharacterType.Consonant),
                 new TranscriptionEntry("x", "𐰽", "𐰾", CharacterType.Consonant),
                 new TranscriptionEntry("c", "𐰳", "𐰙", CharacterType.Consonant),
                 new TranscriptionEntry("v", "𐰉", "𐰌", CharacterType.Consonant),
                 new TranscriptionEntry("nt", "𐰦", "𐰦", CharacterType.Special),
                 new TranscriptionEntry("rt", "𐱈", "𐱈", CharacterType.Special),
                 new TranscriptionEntry("lt", "𐰡", "𐰡", CharacterType.Special),
                 new TranscriptionEntry("nç", "𐰨", "𐰨", CharacterType.Special),
                 new TranscriptionEntry("qı", "𐰶", "𐰶", CharacterType.Special_Hard),
                 new TranscriptionEntry("ıq", "𐰷", "𐰷", CharacterType.Special_Hard),
                 new TranscriptionEntry("ot", "𐱇", "𐱇", CharacterType.Special_Hard),
                 new TranscriptionEntry("oq", "𐰹", "𐰹", CharacterType.Special_Hard),
                 new TranscriptionEntry("uq", "𐰸", "𐰸", CharacterType.Special_Hard),
                 new TranscriptionEntry("ök", "𐰝", "𐰝", CharacterType.Special_Soft),
                 new TranscriptionEntry("ük", "𐰰", "𐰰", CharacterType.Special_Soft),

                 new TranscriptionEntry("өk", "𐰝", "𐰝", CharacterType.Special_Soft),

                 new TranscriptionEntry("ق", "𐰴", "𐰚", CharacterType.Consonant),
                 new TranscriptionEntry("ش", "𐱀", "𐱁", CharacterType.Consonant),
                 new TranscriptionEntry("ە", "𐰅", "𐰅", CharacterType.SoftVowel),
                 new TranscriptionEntry("ر", "𐰺", "𐰼", CharacterType.Consonant),
                 new TranscriptionEntry("ت", "𐱄", "𐱅", CharacterType.Consonant),
                 new TranscriptionEntry("ي", "𐰖", "𐰘", CharacterType.Consonant),
                 new TranscriptionEntry("ۇ", "𐰆", "𐰆", CharacterType.HardVowel),
                 new TranscriptionEntry("ئ", "𐰄", "𐰄", CharacterType.SoftVowel),
                 new TranscriptionEntry("و", "𐰆", "𐰆", CharacterType.HardVowel),
                 new TranscriptionEntry("پ", "𐰯", "𐰯", CharacterType.Consonant_Univ),
                 new TranscriptionEntry("ا", "𐰀", "𐰀", CharacterType.HardVowel),
                 new TranscriptionEntry("س", "𐰽", "𐰾", CharacterType.Consonant),
                 new TranscriptionEntry("د", "𐰑", "𐰓", CharacterType.Consonant),
                 new TranscriptionEntry("گ", "𐰍", "𐰏", CharacterType.Consonant),
                 new TranscriptionEntry("ۉ", "𐰈", "𐰈", CharacterType.SoftVowel),
                 new TranscriptionEntry("ج", "𐰳", "𐰙", CharacterType.Consonant),
                 new TranscriptionEntry("ۅ", "𐰇", "𐰇", CharacterType.SoftVowel),
                 new TranscriptionEntry("ك", "𐰴", "𐰚", CharacterType.Consonant),
                 new TranscriptionEntry("ل", "𐰞", "𐰠", CharacterType.Consonant),
                 new TranscriptionEntry("ع", "𐰍", "𐰏", CharacterType.Consonant),
                 
                 new TranscriptionEntry("ز", "𐰕", "𐰕", CharacterType.Consonant_Univ),
                 new TranscriptionEntry("ڭ", "𐰬", "𐰭", CharacterType.Consonant),
                 new TranscriptionEntry("چ", "𐰲", "𐰲", CharacterType.Consonant_Univ),
                 new TranscriptionEntry("ى", "𐰃", "𐰃", CharacterType.HardVowel),
                 new TranscriptionEntry("ب", "𐰉", "𐰌", CharacterType.Consonant),
                 new TranscriptionEntry("ن", "𐰣", "𐰤", CharacterType.Consonant),
                 new TranscriptionEntry("م", "𐰢", "𐰢", CharacterType.Consonant_Univ),
                 new TranscriptionEntry("ۇ", "𐰆", "𐰈", CharacterType.Consonant),
                 new TranscriptionEntry("ف", "𐰯", "𐰯", CharacterType.Consonant_Univ),
                 new TranscriptionEntry("ح", "𐰴", "𐰚", CharacterType.Consonant),
                 new TranscriptionEntry("ۋ", "𐰉", "𐰌", CharacterType.Consonant),
                 
                 new TranscriptionEntry("نت", "𐰦", "𐰦", CharacterType.Special),
                 new TranscriptionEntry("رت", "𐱈", "𐱈", CharacterType.Special),
                 new TranscriptionEntry("لت", "𐰡", "𐰡", CharacterType.Special),
                 new TranscriptionEntry("نچ", "𐰨", "𐰨", CharacterType.Special),
                 new TranscriptionEntry("قى", "𐰶", "𐰶", CharacterType.Special_Hard),
                 new TranscriptionEntry("ىق", "𐰷", "𐰷", CharacterType.Special_Hard),
                 new TranscriptionEntry("وت", "𐱇", "𐱇", CharacterType.Special_Hard),
                 new TranscriptionEntry("وق", "𐰹", "𐰹", CharacterType.Special_Hard),
                 new TranscriptionEntry("ۇق", "𐰸", "𐰸", CharacterType.Special_Hard),
                 new TranscriptionEntry("ۅك", "𐰝", "𐰝", CharacterType.Special_Soft),
                 new TranscriptionEntry("ۉك", "𐰰", "𐰰", CharacterType.Special_Soft)
             ];
             
             this.transcriptoinUnits = [];
             this.toDelete = [];
         }
         
         GetTranscription(text){
             text = text.replace(/я/gi, "йа");
             text = text.replace(/ю/gi, "йу");
             text = text.replace(/ё/gi, "йо");
             
             this.toDelete.length = 0;
             this.transcriptoinUnits.length = 0;
             
             let result = [];
             let lastVowel = null;
             let lastConsonant = null; // declared but unused in C# logic
             let lastChar = "";
             
             for (let idx = 0; idx < text.length; idx++){
                 const c = text[idx];
                 const cLower = c.toLowerCase();
                 
                 let entry = this._transcriptionList.find(e => e.symbol.toLowerCase() === cLower);
                 let specTamga = null;
                 
                 if (lastChar !== ""){
                     const potential = (lastChar + c).toLowerCase();
                     specTamga = this._transcriptionList.find(e => e.symbol.toLowerCase() === potential);
                 }
                 
                 const transcriptoinUnit = new TranscriptoinUnit();
                 transcriptoinUnit.self = entry;
                 
                 if (specTamga != null){
                     transcriptoinUnit.self = specTamga;
                     
                     if (this.transcriptoinUnits.length > 0){
                         this.transcriptoinUnits[this.transcriptoinUnits.length - 1].self = specTamga;
                     }
                     lastChar = lastChar + c;
                     // no push (matches C#)
                     continue;
                 } else {
                     if (entry != null){
                         switch(entry.type){
                             case CharacterType.HardVowel:
                                 lastVowel = entry;
                                 if (this.transcriptoinUnits.length > 0){
                                     this.transcriptoinUnits[this.transcriptoinUnits.length - 1].SetNextVowel(lastVowel);
                                 }
                                 break;
                             case CharacterType.SoftVowel:
                                 lastVowel = entry;
                                 if (this.transcriptoinUnits.length > 0){
                                     this.transcriptoinUnits[this.transcriptoinUnits.length - 1].SetNextVowel(lastVowel);
                                 }
                                 break;
                             case CharacterType.Consonant:
                             case CharacterType.Consonant_Univ:
                                 lastConsonant = entry;
                                 transcriptoinUnit.SetPreviousVowel(lastVowel);
                                 break;
                             case CharacterType.Special_Hard:
                                 break;
                             case CharacterType.epmty:
                                 break;
                         }
                     } else {
                         transcriptoinUnit.result = c;
                     }
                     
                     this.transcriptoinUnits.push(transcriptoinUnit);
                     lastChar = c;
                 }
             }
             
             for (const unit of this.transcriptoinUnits){
                 unit.Validate();
             }
             
             // RemoveAll(x => x.self == null || string.IsNullOrWhiteSpace(x.result))
             this.transcriptoinUnits = this.transcriptoinUnits.filter(x => !(x.self == null || (x.result == null || x.result.trim() === "")));
             
             // this.TryRemove_A_Special();
             // this.TryRemove_Consonant_A_Consonant();
             // this.TryRemove_Consonant_E_Consonant();
             // this.TryRemove_SoftAfterHard();
             // this.TryRemove_HardAfterSoft();
             
             // transcriptoinUnits.RemoveAll(x => toDelete.Contains(x));
             if (this.toDelete.length){
                 this.transcriptoinUnits = this.transcriptoinUnits.filter(x => this.toDelete.indexOf(x) === -1);
             }
             
             for (const unit of this.transcriptoinUnits) {
                 result.push(unit.result);
             }
             return result.join("") + "\u202C";
         }
         
         TryRemove_A_Special(){
             try{
                 if (this.transcriptoinUnits.length > 2){
                     if (
                         (
                          (this.transcriptoinUnits[0].self.symbol.toLowerCase() === "a")
                          || (this.transcriptoinUnits[0].self.symbol.toLowerCase() === "а")
                          || (this.transcriptoinUnits[0].self.symbol.toLowerCase() === "ا")
                          )
                         && (this.transcriptoinUnits[1].self.type === CharacterType.Special)
                         ){
                             this.transcriptoinUnits.splice(0, 1);
                         }
                 }
             }catch(ex){
                 // Debug.LogError(ex);
             }
         }
         
         TryRemove_Consonant_A_Consonant(){
             try{
                 if (this.transcriptoinUnits.length > 2){
                     const firsTamga = this.transcriptoinUnits[0];
                     const secondTamga = this.transcriptoinUnits[1];
                     const thirdTamga = this.transcriptoinUnits[2];
                     
                     const isNeededFirstConsonant =
                     (firsTamga.isSoft === false) &&
                     (firsTamga.self.type === CharacterType.Consonant ||
                      firsTamga.self.type === CharacterType.Consonant_Univ);
                     if (!isNeededFirstConsonant) return;
                     
                     const isNeededVowel =
                     secondTamga.self.symbol.toLowerCase() === "a" ||
                     secondTamga.self.symbol.toLowerCase() === "а" ||
                     secondTamga.self.symbol.toLowerCase() === "ا";
                     if (!isNeededVowel) return;
                     
                     const isNeededConsonant =
                     (thirdTamga.isSoft === false) &&
                     (thirdTamga.self.type === CharacterType.Consonant ||
                      thirdTamga.self.type === CharacterType.Consonant_Univ ||
                      thirdTamga.self.type === CharacterType.Special ||
                      thirdTamga.self.type === CharacterType.Special_Hard_ConsonantFirst);
                     
                     const isNeededSpecial =
                     (thirdTamga.self.type === CharacterType.Special ||
                      thirdTamga.self.type === CharacterType.Special_Hard_ConsonantFirst);
                     
                     if (isNeededFirstConsonant && isNeededVowel && (isNeededConsonant || isNeededSpecial)){
                         this.toDelete.push(secondTamga);
                     }
                 }
             }catch(ex){ /* no-op */ }
         }
         
         TryRemove_Consonant_E_Consonant(){
             try{
                 if (this.transcriptoinUnits.length > 2){
                     const firsTamga = this.transcriptoinUnits[0];
                     const secondTamga = this.transcriptoinUnits[1];
                     const thirdTamga = this.transcriptoinUnits[2];
                     
                     const isNeededFirstConsonant =
                     (firsTamga.isSoft === true) &&
                     (firsTamga.self.type === CharacterType.Consonant ||
                      firsTamga.self.type === CharacterType.Consonant_Univ);
                     if (!isNeededFirstConsonant) return;
                     
                     const sym2 = (secondTamga.self.symbol || "").toLowerCase();
                     const isNeededVowel = (sym2 === "e" || sym2 === "э" || sym2 === "е" || sym2 === "ە");
                     if (!isNeededVowel) return;
                     
                     const isNeededConsonant =
                     (thirdTamga.isSoft === true) &&
                     (thirdTamga.self.type === CharacterType.Consonant ||
                      thirdTamga.self.type === CharacterType.Consonant_Univ ||
                      thirdTamga.self.type === CharacterType.Special ||
                      thirdTamga.self.type === CharacterType.Special_Soft_ConsonantFirst);
                     
                     const isNeededSpecial =
                     (thirdTamga.self.type === CharacterType.Special ||
                      thirdTamga.self.type === CharacterType.Special_Soft_ConsonantFirst);
                     
                     if (isNeededFirstConsonant && isNeededVowel && (isNeededConsonant || isNeededSpecial)){
                         this.toDelete.push(secondTamga);
                     }
                 }
             }catch(ex){ /* no-op */ }
         }
         
         TryRemove_SoftAfterHard(){
             if (this.transcriptoinUnits.length > 3){
                 let index = 0;
                 for (let i = 0; i < this.transcriptoinUnits.length; i++){
                     if (i + 3 < this.transcriptoinUnits.length){
                         const firsTamga = this.transcriptoinUnits[i];
                         const secondTamga = this.transcriptoinUnits[i+1];
                         const thirdTamga = this.transcriptoinUnits[i+2];
                         const forthTamga = this.transcriptoinUnits[i+3];
                         
                         try{
                             let isFirstHard =
                             (firsTamga.isSoft === false) &&
                             (firsTamga.self.type === CharacterType.Consonant ||
                              firsTamga.self.type === CharacterType.Consonant_Univ ||
                              firsTamga.self.type === CharacterType.Special_Hard_ConsonantFirst ||
                              firsTamga.self.type === CharacterType.Special_Hard_VowelFirst ||
                              firsTamga.self.type === CharacterType.Special);
                             
                             if (firsTamga.self.type === CharacterType.HardVowel) isFirstHard = true;
                             if (firsTamga.self.type === CharacterType.Special_Hard_ConsonantFirst ||
                                 firsTamga.self.type === CharacterType.Special_Hard_VowelFirst) isFirstHard = true;
                             if (!isFirstHard) continue;
                             
                             const isNeededFirstConsonant =
                             (secondTamga.isSoft === true) &&
                             (secondTamga.self.type === CharacterType.Consonant ||
                              secondTamga.self.type === CharacterType.Consonant_Univ);
                             if (!isNeededFirstConsonant) continue;
                             
                             const sym3 = (thirdTamga.self.symbol || "").toLowerCase();
                             const isNeededVowel = (sym3 === "e" || sym3 === "э" || sym3 === "е"|| sym3 === "ە");
                             if (!isNeededVowel) continue;
                             
                             const isNeededConsonant =
                             (forthTamga.isSoft === true) &&
                             (forthTamga.self.type === CharacterType.Consonant ||
                              forthTamga.self.type === CharacterType.Consonant_Univ ||
                              forthTamga.self.type === CharacterType.Special ||
                              forthTamga.self.type === CharacterType.Special_Hard_VowelFirst ||
                              forthTamga.self.type === CharacterType.Special);
                             if (!isNeededConsonant) continue;
                             
                             if (isFirstHard && isNeededFirstConsonant && isNeededVowel && isNeededConsonant){
                                 this.toDelete.push(thirdTamga);
                             }
                         }catch(ex){ /* no-op */ }
                         
                         index = 0;
                     } else {
                         index++;
                     }
                 }
             }
         }
         
         TryRemove_HardAfterSoft(){
             if (this.transcriptoinUnits.length > 3){
                 let index = 0;
                 for (let i = 0; i < this.transcriptoinUnits.length; i++){
                     if (i + 3 < this.transcriptoinUnits.length){
                         const firsTamga = this.transcriptoinUnits[i];
                         const secondTamga = this.transcriptoinUnits[i+1];
                         const thirdTamga = this.transcriptoinUnits[i+2];
                         const forthTamga = this.transcriptoinUnits[i+3];
                         
                         try{
                             let isFirstHard =
                             (firsTamga.isSoft === true) &&
                             (firsTamga.self.type === CharacterType.Consonant ||
                              firsTamga.self.type === CharacterType.Consonant_Univ ||
                              firsTamga.self.type === CharacterType.Special ||
                              firsTamga.self.type === CharacterType.Special_Soft_ConsonantFirst ||
                              firsTamga.self.type === CharacterType.Special_Soft_VowelFirst);
                             
                             if (firsTamga.self.type === CharacterType.SoftVowel) isFirstHard = true;
                             if (firsTamga.self.type === CharacterType.Special_Soft_ConsonantFirst ||
                                 firsTamga.self.type === CharacterType.Special_Soft_VowelFirst) isFirstHard = true;
                             if (!isFirstHard) continue;
                             
                             const isNeededFirstConsonant =
                             (secondTamga.isSoft === false) &&
                             (secondTamga.self.type === CharacterType.Consonant ||
                              secondTamga.self.type === CharacterType.Consonant_Univ);
                             if (!isNeededFirstConsonant) continue;
                             
                             const sym3 = (thirdTamga.self.symbol || "").toLowerCase();
                             const isNeededVowel = (sym3 === "a" || sym3 === "а" || sym3 === "ا");
                             if (!isNeededVowel) continue;
                             
                             const isNeededConsonant =
                             (forthTamga.isSoft === false) &&
                             (forthTamga.self.type === CharacterType.Consonant ||
                              forthTamga.self.type === CharacterType.Consonant_Univ ||
                              forthTamga.self.type === CharacterType.Special ||
                              forthTamga.self.type === CharacterType.Special_Soft_ConsonantFirst ||
                              forthTamga.self.type === CharacterType.Special_Soft_VowelFirst);
                             if (!isNeededConsonant) continue;
                             
                             if (isFirstHard && isNeededFirstConsonant && isNeededVowel && isNeededConsonant){
                                 this.toDelete.push(thirdTamga);
                             }
                         }catch(ex){ /* no-op */ }
                         
                         index = 0;
                     } else {
                         index++;
                     }
                 }
             }
         }
     }
     
     // export on global like a Unity-style singleton-ish script would attach to window
     global.Transcrptiber_Old = Transcrptiber_Old;
     global.CharacterType_Old = CharacterType;
 })(typeof window !== "undefined" ? window : globalThis);
