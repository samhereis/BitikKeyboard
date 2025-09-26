(function(global){
  // --- Enum (typos preserved) ---
  const CharacterType = {
    epmty: "epmty",
    Consonant: "Consonant",
    Consonant_Univ: "Consonant_Univ",
    HardVowel: "HardVowel",
    SoftVowel: "SoftVowel",
    Special: "Special",
    Special_Hard: "Special_Hard",
    Special_Soft: "Special_Soft",
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
            new TranscriptionEntry("у", "𐰩", "𐰩", CharacterType.HardVowel),
            new TranscriptionEntry("к", "𐰴", "𐰚", CharacterType.Consonant),
            new TranscriptionEntry("е", "𐰅", "𐰅", CharacterType.SoftVowel),
            new TranscriptionEntry("н", "𐰣", "𐰤", CharacterType.Consonant),
            new TranscriptionEntry("г", "𐰍", "𐰏", CharacterType.Consonant),
            new TranscriptionEntry("ш", "𐱀", "𐱁", CharacterType.Consonant),
            new TranscriptionEntry("ү", "𐰈", "𐰈", CharacterType.SoftVowel),
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
            new TranscriptionEntry("я", "𐰖𐰀", "𐰖𐰀", CharacterType.epmty),
            new TranscriptionEntry("ю", "𐰖𐰆", "𐰖𐰆", CharacterType.epmty),
            new TranscriptionEntry("ё", "𐰖𐰆", "𐰖𐰆", CharacterType.epmty),
            new TranscriptionEntry("ф", "𐰯", "𐰯", CharacterType.Consonant_Univ),
            new TranscriptionEntry("в", "𐰉", "𐰌", CharacterType.Consonant),
            new TranscriptionEntry("х", "𐰴", "𐰚", CharacterType.Consonant),
            new TranscriptionEntry("ц", "𐰽", "𐰾", CharacterType.Consonant),
            new TranscriptionEntry("щ", "𐱀", "𐱁", CharacterType.Consonant),
            new TranscriptionEntry("нт", "𐰦", "𐰦", CharacterType.Special),
            new TranscriptionEntry("рт", "𐱈", "𐱈", CharacterType.Special),
            new TranscriptionEntry("лт", "𐰡", "𐰡", CharacterType.Special),
            new TranscriptionEntry("нч", "𐰨", "𐰨", CharacterType.Special),
            new TranscriptionEntry("чы", "𐰱", "𐰱", CharacterType.Special_Hard),
            new TranscriptionEntry("чи", "𐰱", "𐰱", CharacterType.Special_Soft),
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
            new TranscriptionEntry("u", "𐰩", "𐰩", CharacterType.HardVowel),
            new TranscriptionEntry("i", "𐰄", "𐰄", CharacterType.SoftVowel),
            new TranscriptionEntry("o", "𐰆", "𐰆", CharacterType.HardVowel),
            new TranscriptionEntry("p", "𐰯", "𐰯", CharacterType.Consonant_Univ),
            new TranscriptionEntry("a", "𐰀", "𐰀", CharacterType.HardVowel),
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
            new TranscriptionEntry("çı", "𐰱", "𐰱", CharacterType.Special_Hard),
            new TranscriptionEntry("çi", "𐰱", "𐰱", CharacterType.Special_Soft),
            new TranscriptionEntry("ot", "𐱇", "𐱇", CharacterType.Special_Hard),
            new TranscriptionEntry("oq", "𐰹", "𐰹", CharacterType.Special_Hard),
            new TranscriptionEntry("uq", "𐰸", "𐰸", CharacterType.Special_Hard),
            new TranscriptionEntry("ök", "𐰝", "𐰝", CharacterType.Special_Soft),
            new TranscriptionEntry("üк", "𐰰", "𐰰", CharacterType.Special_Soft),
      ];

      this.transcriptoinUnits = [];
      this.toDelete = [];
    }

    GetTranscription(text){
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

      this.TryRemove_A_Special();
      this.TryRemove_Consonant_A_Consonant();
      this.TryRemove_Consonant_E_Consonant();
      this.TryRemove_SoftAfterHard();
      this.TryRemove_HardAfterSoft();

      // transcriptoinUnits.RemoveAll(x => toDelete.Contains(x));
      if (this.toDelete.length){
        this.transcriptoinUnits = this.transcriptoinUnits.filter(x => this.toDelete.indexOf(x) === -1);
      }

      for (const unit of this.transcriptoinUnits) {
        result.push(unit.result);
      }
      return "\u202B" + result.join("") + "\u202C";
    }

    TryRemove_A_Special(){
      try{
        if (this.transcriptoinUnits.length > 2){
          if (
            (
              (this.transcriptoinUnits[0].self.symbol.toLowerCase() === "a")
              || (this.transcriptoinUnits[0].self.symbol.toLowerCase() === "а")
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
            (
              firsTamga.self.type === CharacterType.Consonant ||
              firsTamga.self.type === CharacterType.Consonant_Univ
            );
          if (isNeededFirstConsonant === false){ return; }

          const isNeededVowel =
            secondTamga.self.symbol.toLowerCase() === "a" ||
            secondTamga.self.symbol.toLowerCase() === "а";
          if (isNeededVowel === false){ return; }

          const isNeededConsonant =
            (thirdTamga.isSoft === false) &&
            (
              thirdTamga.self.type === CharacterType.Consonant ||
              thirdTamga.self.type === CharacterType.Consonant_Univ ||
              thirdTamga.self.type === CharacterType.Special ||
              thirdTamga.self.type === CharacterType.Special_Hard
            );
          if (isNeededConsonant === false){ return; }

          if (isNeededFirstConsonant && isNeededVowel && isNeededConsonant){
            this.transcriptoinUnits.splice(1,1);
          }
        }
      }catch(ex){
        // Debug.LogError(ex);
      }
    }

    TryRemove_Consonant_E_Consonant(){
      try{
        if (this.transcriptoinUnits.length > 2){
          const firsTamga = this.transcriptoinUnits[0];
          const secondTamga = this.transcriptoinUnits[1];
          const thirdTamga = this.transcriptoinUnits[2];

          const isNeededFirstConsonant =
            (firsTamga.isSoft === true) &&
            (
              firsTamga.self.type === CharacterType.Consonant ||
              firsTamga.self.type === CharacterType.Consonant_Univ
            );
          if (isNeededFirstConsonant === false){ return; }

          const sym2 = secondTamga.self.symbol.toLowerCase();
          const isNeededVowel = (sym2 === "e" || sym3 === "е" || sym3 === "э");
          if (isNeededVowel === false){ return; }

          const isNeededConsonant =
            (thirdTamga.isSoft === true) &&
            (
              thirdTamga.self.type === CharacterType.Consonant ||
              thirdTamga.self.type === CharacterType.Consonant_Univ
            );
          if (isNeededConsonant === false){ return; }

          const isNeededSpecial =
            (
              thirdTamga.self.type === CharacterType.Special ||
              thirdTamga.self.type === CharacterType.Consonant_Univ ||
              thirdTamga.self.type === CharacterType.Special_Soft
            );
          if (isNeededSpecial === false){ return; }

          if (isNeededFirstConsonant && isNeededVowel && (isNeededConsonant || isNeededSpecial)){
            this.transcriptoinUnits.splice(1,1);
          }
        }
      }catch(ex){
        // Debug.LogError(ex);
      }
    }

    TryRemove_SoftAfterHard(){
      if (this.transcriptoinUnits.length > 3){
        let index = 0;

        for (let i = 0; i < this.transcriptoinUnits.length; i++){
          if (i + 3 < this.transcriptoinUnits.length){
            const first = i;
            const second = i + 1;
            const third = i + 2;
            const forth = i + 3;

            const firsTamga = this.transcriptoinUnits[first];
            const secondTamga = this.transcriptoinUnits[second];
            const thirdTamga = this.transcriptoinUnits[third];
            const forthTamga = this.transcriptoinUnits[forth];

            try{
              let isFirstHard =
                (firsTamga.isSoft === false) &&
                (
                  firsTamga.self.type === CharacterType.Consonant ||
                  firsTamga.self.type === CharacterType.Consonant_Univ ||
                  firsTamga.self.type === CharacterType.Special_Hard
                );

              if (firsTamga.self.type === CharacterType.HardVowel) { isFirstHard = true; }
              if (firsTamga.self.type === CharacterType.Special_Hard) { isFirstHard = true; }
              if (isFirstHard === false){ continue; }

              const isNeededFirstConsonant =
                (secondTamga.isSoft === true) &&
                (
                  secondTamga.self.type === CharacterType.Consonant ||
                  secondTamga.self.type === CharacterType.Consonant_Univ
                );
              if (isNeededFirstConsonant === false){ continue; }

              const sym3 = (thirdTamga.self.symbol || "").toLowerCase();
              const isNeededVowel = (sym3 === "e" || sym3 === "е" || sym3 === "э");
              if (isNeededVowel === false){ continue; }

              const isNeededConsonant =
                (forthTamga.isSoft === true) &&
                (
                  forthTamga.self.type === CharacterType.Consonant ||
                  forthTamga.self.type === CharacterType.Consonant_Univ ||
                  forthTamga.self.type === CharacterType.Special ||
                  forthTamga.self.type === CharacterType.Special_Hard
                );
              if (isNeededConsonant === false){ continue; }

              if (isFirstHard && isNeededFirstConsonant && isNeededVowel && isNeededConsonant){
                this.toDelete.push(thirdTamga);
              }
            }catch(ex){
              // Debug.LogError(ex);
            }

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
            const first = i;
            const second = i + 1;
            const third = i + 2;
            const forth = i + 3;

            const firsTamga = this.transcriptoinUnits[first];
            const secondTamga = this.transcriptoinUnits[second];
            const thirdTamga = this.transcriptoinUnits[third];
            const forthTamga = this.transcriptoinUnits[forth];

            try{
              let isFirstHard =
                (firsTamga.isSoft === true) &&
                (
                  firsTamga.self.type === CharacterType.Consonant ||
                  firsTamga.self.type === CharacterType.Consonant_Univ ||
                  firsTamga.self.type === CharacterType.Special_Soft
                );

              if (firsTamga.self.type === CharacterType.SoftVowel) { isFirstHard = true; }
              if (firsTamga.self.type === CharacterType.Special_Soft) { isFirstHard = true; }
              if (isFirstHard === false){ continue; }

              const isNeededFirstConsonant =
                (secondTamga.isSoft === false) &&
                (
                  secondTamga.self.type === CharacterType.Consonant ||
                  secondTamga.self.type === CharacterType.Consonant_Univ
                );
              if (isNeededFirstConsonant === false){ continue; }

              const sym3 = (thirdTamga.self.symbol || "").toLowerCase();
              const isNeededVowel = (sym3 === "a" || sym3 === "а");
              if (isNeededVowel === false){ continue; }

              const isNeededConsonant =
                (forthTamga.isSoft === false) &&
                (
                  forthTamga.self.type === CharacterType.Consonant ||
                  forthTamga.self.type === CharacterType.Consonant_Univ ||
                  forthTamga.self.type === CharacterType.Special ||
                  forthTamga.self.type === CharacterType.Special_Hard
                );
              if (isNeededConsonant === false){ continue; }

              if (isFirstHard && isNeededFirstConsonant && isNeededVowel && isNeededConsonant){
                this.toDelete.push(thirdTamga);
              }
            }catch(ex){
              // Debug.LogError(ex);
            }

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