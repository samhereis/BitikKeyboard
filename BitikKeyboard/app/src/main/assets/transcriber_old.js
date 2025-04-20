// Helpers for surrogate pairs and Unicode
function isSurrogate(text, index) {
    var code = text.charCodeAt(index);
    return code >= 0xD800 && code <= 0xDFFF;
}
function isSurrogatePair(text, index) {
    if (index + 1 >= text.length) return false;
    var high = text.charCodeAt(index), low = text.charCodeAt(index + 1);
    return high >= 0xD800 && high <= 0xDBFF && low >= 0xDC00 && low <= 0xDFFF;
}
function getUnicodeCategory(ch) {
    return "Other";
}
var UnicodeCategory = { Format: "Format" };

// Enum
var CharacterType = Object.freeze({
    epmty: 0,
    Character: 1,
    HardCharacter: 2,
    SoftCharacter: 3,
    HardConsonant: 4,
    HardConsonant_Single: 5,
    SoftConsonant: 6,
    HardVowel: 7,
    HardVowel_HasSoftVariant: 8,
    SoftVowel: 9
});

// TranscriptionEntry (normal + copy constructor)
function TranscriptionEntry(symbolOrEntry, transcription, type, transcriptionAlternative) {
    if (symbolOrEntry instanceof TranscriptionEntry) {
        var toCopy = symbolOrEntry;
        this.symbol = toCopy.symbol;
        this.transcription = toCopy.transcription;
        this.transcriptionAlternative = toCopy.transcriptionAlternative;
        this.type = toCopy.type;
        this.vowel = toCopy.vowel;
        this.vowel_Alternative = toCopy.vowel_Alternative;
        return;
    }
    this.recheck = null;
    this.symbol = symbolOrEntry;
    this.transcription = transcription;
    this.transcriptionAlternative = transcriptionAlternative || "";
    this.type = type;
    this.vowel = null;
    this.vowel_Alternative = null;
    this.isAlternative = false;
    this.final = "";
}
TranscriptionEntry.prototype.GetVowel = function(isAlternative) {
    return isAlternative ? this.vowel_Alternative : this.vowel;
};

// SingleTranscriptionData
function SingleTranscriptionData() {
    this.onFistVowelSet = null;
    this.onFistConsonantSet = null;
    this.onLastVowelSet = null;
    this.onLastConsonantSet = null;
    this.firstVowel = null;
    this.firstConsonant = null;
    this.lastVowel = null;
    this.lastConsonant = null;
    this.allProcessed = [];
}
SingleTranscriptionData.prototype.SetLastVowel = function(newVowel) {
    if (!this.firstVowel) {
        this.firstVowel = newVowel;
        if (this.onFistVowelSet) this.onFistVowelSet(newVowel);
    }
    this.lastVowel = newVowel;
    if (this.onLastVowelSet) this.onLastVowelSet(newVowel);
};
SingleTranscriptionData.prototype.SetLastConsonant = function(newConsonant) {
    if (!this.firstConsonant) {
        this.firstConsonant = newConsonant;
        if (this.onFistConsonantSet) this.onFistConsonantSet(newConsonant);
    }
    this.lastConsonant = newConsonant;
    if (this.onLastConsonantSet) this.onLastConsonantSet(newConsonant);
};

// Private helper
function GetTranscriptionEntry(arg) {
    var t = arg.transcription;
    if (arg.isAlternative && arg.transcriptionAlternative) t = arg.transcriptionAlternative;
    return t;
}

// Processor functions
function ProcessCharacter(arg) {
    arg.final = GetTranscriptionEntry(arg);
    return arg.final;
}
function ProcessHardCharacter(arg) {
    var t = GetTranscriptionEntry(arg);
    this.singleTranscriptionData.SetLastConsonant(arg);
    this.singleTranscriptionData.lastVowel = arg.GetVowel(arg.isAlternative);
    arg.final = t;
    return arg.final;
}
function ProcessSoftCharacter(arg) {
    return ProcessHardCharacter.call(this, arg);
}
function ProcessHardConsonant(arg) {
    var t = GetTranscriptionEntry(arg);
    this.singleTranscriptionData.SetLastConsonant(arg);
    arg.final = t.slice(-1);
    return arg.final;
}
function ProcessHardConsonant_Single(arg) {
    arg.final = GetTranscriptionEntry(arg).slice(-1);
    return arg.final;
}
function ProcessSoftConsonant(arg) {
    return ProcessHardConsonant.call(this, arg);
}
function ProcessHardVowel(arg) {
    this.singleTranscriptionData.SetLastVowel(arg);
    arg.final = GetTranscriptionEntry(arg);
    return arg.final;
}
function ProcessHardVowel_HasSoftVariant(arg) {
    this.singleTranscriptionData.SetLastVowel(arg);
    var t = arg.transcription;
    if (arg.transcriptionAlternative) {
        if (this.singleTranscriptionData.lastConsonant) {
            if (this.singleTranscriptionData.lastConsonant.type === CharacterType.SoftConsonant) {
                t = arg.transcriptionAlternative;
            }
        } else {
            var self = this;
            arg.recheck = function() {
                var fc = self.singleTranscriptionData.firstConsonant;
                if (fc) {
                    arg.final = (fc.type === CharacterType.SoftConsonant)
                        ? arg.transcriptionAlternative
                        : arg.transcription;
                }
            };
        }
    }
    arg.final = t;
    return arg.final;
}
function ProcessSoftVowel(arg) {
    if (!this.singleTranscriptionData.lastConsonant) {
        this.singleTranscriptionData.SetLastVowel(arg);
    }
    this.singleTranscriptionData.lastVowel = arg;
    arg.final = GetTranscriptionEntry(arg);
    return arg.final;
}

// Main class
function CorrentText_Old() {
    this._isInitialized = false;
    this._transcriptionList = [
        new TranscriptionEntry("", "", CharacterType.epmty, ""),

        // First Row
        new TranscriptionEntry("𐰶", "ҚЫ", CharacterType.HardCharacter, "ЫҚ"),
        new TranscriptionEntry("𐰷", "ЫҚ", CharacterType.HardCharacter, "ҚЫ"),
        new TranscriptionEntry("𐰬", "аҢ", CharacterType.HardConsonant),
        new TranscriptionEntry("𐰧", "аҢ", CharacterType.HardConsonant),
        new TranscriptionEntry("𐰁", "А", CharacterType.HardVowel_HasSoftVariant, "E"),
        new TranscriptionEntry("𐰀", "А", CharacterType.HardVowel_HasSoftVariant, "E"),
        new TranscriptionEntry("𐰺", "аР", CharacterType.HardConsonant),
        new TranscriptionEntry("𐱄", "аТ", CharacterType.HardConsonant),
        new TranscriptionEntry("𐱃", "аТ", CharacterType.HardConsonant),
        new TranscriptionEntry("𐱇", "ОТ", CharacterType.HardCharacter, "O"),
        new TranscriptionEntry("𐰖", "аЖ", CharacterType.HardConsonant, "аЙ"),
        new TranscriptionEntry("𐰗", "аЖ", CharacterType.HardConsonant, "аЙ"),
        new TranscriptionEntry("𐰆", "У", CharacterType.HardVowel, "О"),
        new TranscriptionEntry("𐰃", "Ы", CharacterType.HardVowel_HasSoftVariant, "И"),
        new TranscriptionEntry("𐰱", "ЧЫ", CharacterType.HardCharacter, "ЧИ"),
        new TranscriptionEntry("𐰯", "П", CharacterType.HardConsonant_Single),

        // First Row - Shift
        new TranscriptionEntry("𐰭", "еҢ", CharacterType.SoftConsonant),
        new TranscriptionEntry("𐰮", "еҢ", CharacterType.SoftConsonant),
        new TranscriptionEntry("𐰅", "Е", CharacterType.SoftVowel),
        new TranscriptionEntry("𐰂", "Е", CharacterType.SoftVowel),
        new TranscriptionEntry("𐰼", "еР", CharacterType.SoftConsonant),
        new TranscriptionEntry("𐱅", "еТ", CharacterType.SoftConsonant),
        new TranscriptionEntry("𐰘", "еЖ", CharacterType.SoftConsonant, "еЙ"),
        new TranscriptionEntry("𐰙", "еЖ", CharacterType.SoftConsonant, "еЙ"),
        new TranscriptionEntry("𐰇", "Ү", CharacterType.SoftVowel, "Ө"),
        new TranscriptionEntry("𐰈", "Ө", CharacterType.SoftVowel, "Ү"),
        new TranscriptionEntry("𐰄", "И", CharacterType.SoftVowel),

        // Second Row
        new TranscriptionEntry("𐰹", "ОҚ", CharacterType.HardCharacter, "УҚ"),
        new TranscriptionEntry("𐰸", "УҚ", CharacterType.HardCharacter, "ОҚ"),
        new TranscriptionEntry("𐱂", "аС", CharacterType.HardConsonant),
        new TranscriptionEntry("𐰽", "аС", CharacterType.HardConsonant),
        new TranscriptionEntry("𐰑", "аД", CharacterType.HardConsonant),
        new TranscriptionEntry("𐰒", "аД", CharacterType.HardConsonant),
        new TranscriptionEntry("𐰦", "НТ", CharacterType.Character),
        new TranscriptionEntry("𐰍", "аҒ", CharacterType.HardConsonant),
        new TranscriptionEntry("𐰎", "аҒ", CharacterType.HardConsonant),
        new TranscriptionEntry("𐱈", "РТ", CharacterType.Character),
        new TranscriptionEntry("𐰴", "аҚ", CharacterType.HardConsonant),
        new TranscriptionEntry("𐰞", "аЛ", CharacterType.HardConsonant),
        new TranscriptionEntry("𐰟", "аЛ", CharacterType.HardConsonant),

        // Second Row - Shift
        new TranscriptionEntry("𐰾", "еС", CharacterType.SoftConsonant),
        new TranscriptionEntry("𐰓", "еД", CharacterType.SoftConsonant),
        new TranscriptionEntry("𐰨", "НЧ", CharacterType.Character),
        new TranscriptionEntry("𐰏", "еГ", CharacterType.SoftConsonant),
        new TranscriptionEntry("𐰐", "еГ", CharacterType.SoftConsonant),
        new TranscriptionEntry("𐰡", "ЛТ", CharacterType.Character),
        new TranscriptionEntry("𐰚", "еК", CharacterType.SoftConsonant),
        new TranscriptionEntry("𐰠", "еЛ", CharacterType.SoftConsonant),

        // Third Row
        new TranscriptionEntry("𐰕", "З", CharacterType.HardConsonant_Single),
        new TranscriptionEntry("𐱀", "Ш", CharacterType.HardConsonant_Single),
        new TranscriptionEntry("𐰿", "Ш", CharacterType.HardConsonant_Single),
        new TranscriptionEntry("𐱁", "Ш", CharacterType.HardConsonant_Single),
        new TranscriptionEntry("𐰲", "Ч", CharacterType.HardConsonant_Single),
        new TranscriptionEntry("𐰳", "Ч", CharacterType.HardConsonant_Single),
        new TranscriptionEntry("𐰜", "ҮК", CharacterType.SoftCharacter, "ӨК"),
        new TranscriptionEntry("𐰝", "ӨК", CharacterType.SoftCharacter, "ҮК"),
        new TranscriptionEntry("𐰉", "аБ", CharacterType.HardConsonant),
        new TranscriptionEntry("𐰊", "аБ", CharacterType.HardConsonant),
        new TranscriptionEntry("𐰣", "аН", CharacterType.HardConsonant),
        new TranscriptionEntry("𐰢", "М", CharacterType.HardConsonant_Single),

        // Third Row - Shift
        new TranscriptionEntry("𐰌", "еБ", CharacterType.SoftConsonant),
        new TranscriptionEntry("𐰋", "еБ", CharacterType.SoftConsonant),
        new TranscriptionEntry("𐰤", "еН", CharacterType.SoftConsonant),
        new TranscriptionEntry("𐰥", "еН", CharacterType.SoftConsonant)
    ];
    this.singleTranscriptionData = new SingleTranscriptionData();
}

CorrentText_Old.prototype.Initialize = function() {
    if (this._isInitialized) return;
    this._isInitialized = true;

    var o   = this._transcriptionList.find(x => x.transcription === "У");
    var oe  = this._transcriptionList.find(x => x.transcription === "Ө");
    var qi  = this._transcriptionList.find(x => x.transcription === "ҚЫ");
    var iq  = this._transcriptionList.find(x => x.transcription === "ЫҚ");
    var oq  = this._transcriptionList.find(x => x.transcription === "ОҚ");
    var uq  = this._transcriptionList.find(x => x.transcription === "УҚ");
    var oeq = this._transcriptionList.find(x => x.transcription === "ӨК");
    var ueq = this._transcriptionList.find(x => x.transcription === "ҮК");
};

CorrentText_Old.prototype.GetTranscription = function(inputText) {
    return this._GetTranscription(inputText, false);
};
CorrentText_Old.prototype.GetTranscription_Alternative = function(inputText) {
    return this._GetTranscription(inputText, true);
};

CorrentText_Old.prototype._GetTranscription = function(text, isAlt) {
    this.Initialize();
    this.singleTranscriptionData = new SingleTranscriptionData();
    this.singleTranscriptionData.onFistVowelSet = function(x) {
        x.final = GetTranscriptionEntry(x);
    };
    this.singleTranscriptionData.onFistConsonantSet = function(x) {
        x.final = (x.type === CharacterType.SoftConsonant)
            ? x.transcriptionAlternative
            : x.transcription;
    };

    var sb = [], idx = 0, lastArg = this._transcriptionList[0];
    while (idx < text.length) {
        if (isSurrogate(text, idx) && !isSurrogatePair(text, idx)) { idx++; continue; }
        var cp  = text.codePointAt(idx), sym = String.fromCodePoint(cp);
        var orig = this._transcriptionList.find(e => e.symbol === sym);
        if (!orig) {
            sb.push(sym);
            idx += sym.length;
            continue;
        }

        // clone for output
        var entry = new TranscriptionEntry(orig);
        entry.isAlternative = !!isAlt;
        this.singleTranscriptionData.allProcessed.push(entry);

        // process on original for state
        orig.isAlternative = entry.isAlternative;
        if (getUnicodeCategory(sym) !== UnicodeCategory.Format) {
            switch (orig.type) {
                case CharacterType.Character:           ProcessCharacter.call(this, orig); break;
                case CharacterType.HardCharacter:       ProcessHardCharacter.call(this, orig); break;
                case CharacterType.SoftCharacter:       ProcessSoftCharacter.call(this, orig); break;
                case CharacterType.HardConsonant:       ProcessHardConsonant.call(this, orig); break;
                case CharacterType.HardConsonant_Single:ProcessHardConsonant_Single.call(this, orig); break;
                case CharacterType.SoftConsonant:       ProcessSoftConsonant.call(this, orig); break;
                case CharacterType.HardVowel:           ProcessHardVowel.call(this, orig); break;
                case CharacterType.HardVowel_HasSoftVariant:
                    ProcessHardVowel_HasSoftVariant.call(this, orig); break;
                case CharacterType.SoftVowel:           ProcessSoftVowel.call(this, orig); break;
            }
        }
        lastArg = orig;
        idx += sym.length;
    }

    // recheck deferred vowel if needed
    var fv = this.singleTranscriptionData.firstVowel;
    if (fv && fv.recheck) fv.recheck();

    // append finals
    this.singleTranscriptionData.allProcessed.forEach(e => sb.push(e.final));
    return sb.join('');
};