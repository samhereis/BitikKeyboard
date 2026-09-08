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

// TranscriptionEntry constructors
function TranscriptionEntry(symbol, transcription, type, transcriptionAlternative) {
    this.symbol = symbol;
    this.transcription = transcription;
    this.transcriptionAlternative = transcriptionAlternative || "";
    this.type = type;
    this.vowel = null;
    this.vowel_Alternative = null;
    this.isAlternative = false;
    this.final = "";
    this.recheck = null;
}

function TranscriptionEntry_Copy(toCopy) {
    TranscriptionEntry.call(this, toCopy.symbol, toCopy.transcription, toCopy.type, toCopy.transcriptionAlternative);
    this.vowel = toCopy.vowel;
    this.vowel_Alternative = toCopy.vowel_Alternative;
}
TranscriptionEntry_Copy.prototype = Object.create(TranscriptionEntry.prototype);
TranscriptionEntry_Copy.prototype.constructor = TranscriptionEntry_Copy;

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
SingleTranscriptionData.prototype.SetLastVowel = function(newFirstVowel) {
    if (!this.firstVowel) {
        this.firstVowel = newFirstVowel;
        if (this.onFistVowelSet) this.onFistVowelSet(newFirstVowel);
    }
    this.lastVowel = newFirstVowel;
    if (this.onLastVowelSet) this.onLastVowelSet(newFirstVowel);
};
SingleTranscriptionData.prototype.SetLastConsonant = function(newLastConsonant) {
    if (!this.firstConsonant) {
        this.firstConsonant = newLastConsonant;
        if (this.onFistConsonantSet) this.onFistConsonantSet(newLastConsonant);
    }
    this.lastConsonant = newLastConsonant;
    if (this.onLastConsonantSet) this.onLastConsonantSet(newLastConsonant);
};

// Private helper
function GetTranscriptionEntry(arg) {
    var t = arg.transcription;
    if (arg.isAlternative && arg.transcriptionAlternative) {
        t = arg.transcriptionAlternative;
    }
    return t;
}

// Processor functions
function ProcessCharacter(currentArgument, lastArgument) {
    currentArgument.final = GetTranscriptionEntry(currentArgument);
    return currentArgument.final;
}
function ProcessHardCharacter(currentArgument, lastArgument) {
    var transcription = GetTranscriptionEntry(currentArgument);
    this.singleTranscriptionData.SetLastConsonant(currentArgument);
    this.singleTranscriptionData.lastVowel = currentArgument.GetVowel(currentArgument.isAlternative);
    currentArgument.final = transcription;
    return currentArgument.final;
}
function ProcessSoftCharacter(currentArgument, lastArgument) {
    return ProcessHardCharacter.call(this, currentArgument, lastArgument);
}
function ProcessHardConsonant(currentArgument, lastArgument) {
    var transcription = GetTranscriptionEntry(currentArgument);
    this.singleTranscriptionData.SetLastConsonant(currentArgument);
    currentArgument.final = transcription.charAt(transcription.length - 1);
    return currentArgument.final;
}
function ProcessHardConsonant_Single(currentArgument, lastArgument) {
    var transcription = GetTranscriptionEntry(currentArgument);
    currentArgument.final = transcription.charAt(transcription.length - 1);
    return currentArgument.final;
}
function ProcessSoftConsonant(currentArgument, lastArgument) {
    return ProcessHardConsonant.call(this, currentArgument, lastArgument);
}
function ProcessHardVowel(currentArgument, lastArgument) {
    this.singleTranscriptionData.SetLastVowel(currentArgument);
    var transcription = GetTranscriptionEntry(currentArgument);
    currentArgument.final = transcription;
    return currentArgument.final;
}
function ProcessHardVowel_HasSoftVariant(currentArgument, lastArgument) {
    this.singleTranscriptionData.SetLastVowel(currentArgument);
    var transcription = currentArgument.transcription;
    if (currentArgument.transcriptionAlternative) {
        if (this.singleTranscriptionData.lastConsonant) {
            switch (this.singleTranscriptionData.lastConsonant.type) {
                case CharacterType.SoftConsonant:
                    transcription = currentArgument.transcriptionAlternative;
                    break;
                default:
                    transcription = currentArgument.transcription;
            }
        } else {
            var self = this;
            currentArgument.recheck = function() {
                if (self.singleTranscriptionData.firstConsonant) {
                    switch (self.singleTranscriptionData.firstConsonant.type) {
                        case CharacterType.SoftConsonant:
                            currentArgument.final = currentArgument.transcriptionAlternative;
                            break;
                        default:
                            currentArgument.final = currentArgument.transcription;
                    }
                }
            };
        }
    }
    currentArgument.final = transcription;
    return currentArgument.final;
}
function ProcessSoftVowel(currentArgument, lastArgument) {
    if (!this.singleTranscriptionData.lastConsonant) {
        this.singleTranscriptionData.SetLastVowel(currentArgument);
    }
    this.singleTranscriptionData.lastVowel = currentArgument;
    currentArgument.final = GetTranscriptionEntry(currentArgument);
    return currentArgument.final;
}

// Main class
function CorrentText_Old() {
    this._isInitialized = false;
    this._transcriptionList = [
        new TranscriptionEntry("", "", CharacterType.epmty, ""),
        
        // First Row
        new TranscriptionEntry("𐰶", "QI", CharacterType.HardCharacter, "IQ"),
        new TranscriptionEntry("𐰷", "IQ", CharacterType.HardCharacter, "QI"),
        new TranscriptionEntry("𐰬", "aÑ", CharacterType.HardConsonant),
        new TranscriptionEntry("𐰧", "aÑ", CharacterType.HardConsonant),
        new TranscriptionEntry("𐰁", "A", CharacterType.HardVowel_HasSoftVariant, "E"),
        new TranscriptionEntry("𐰀", "A", CharacterType.HardVowel_HasSoftVariant, "E"),
        new TranscriptionEntry("𐰺", "aR", CharacterType.HardConsonant),
        new TranscriptionEntry("𐰻", "aR", CharacterType.HardConsonant),
        new TranscriptionEntry("𐱄", "aT", CharacterType.HardConsonant),
        new TranscriptionEntry("𐱃", "aT", CharacterType.HardConsonant),
        new TranscriptionEntry("𐱇", "OT", CharacterType.HardCharacter),
        new TranscriptionEntry("𐰖", "aJ", CharacterType.HardConsonant, "aY"),
        new TranscriptionEntry("𐰗", "aJ", CharacterType.HardConsonant, "aY"),
        new TranscriptionEntry("𐰆", "U", CharacterType.HardVowel, "O"),
        new TranscriptionEntry("𐰃", "I", CharacterType.HardVowel_HasSoftVariant, "İ"),
        new TranscriptionEntry("𐰱", "ÇI", CharacterType.HardCharacter, "Çİ"),
        new TranscriptionEntry("𐰯", "P", CharacterType.HardConsonant_Single),
        
        // First Row - Shift
        new TranscriptionEntry("𐰭", "eÑ", CharacterType.SoftConsonant),
        new TranscriptionEntry("𐰮", "eÑ", CharacterType.SoftConsonant),
        new TranscriptionEntry("𐰅", "E", CharacterType.SoftVowel),
        new TranscriptionEntry("𐰂", "E", CharacterType.SoftVowel),
        new TranscriptionEntry("𐰼", "eR", CharacterType.SoftConsonant),
        new TranscriptionEntry("𐱅", "eT", CharacterType.SoftConsonant),
        new TranscriptionEntry("𐰘", "eJ", CharacterType.SoftConsonant, "eY"),
        new TranscriptionEntry("𐰙", "eJ", CharacterType.SoftConsonant, "eY"),
        new TranscriptionEntry("𐰇", "Ü", CharacterType.SoftVowel, "Ö"),
        new TranscriptionEntry("𐰈", "Ö", CharacterType.SoftVowel, "Ü"),
        new TranscriptionEntry("𐰄", "İ", CharacterType.SoftVowel),
        
        // Second Row
        new TranscriptionEntry("𐰹", "OQ", CharacterType.HardCharacter, "UQ"),
        new TranscriptionEntry("𐰸", "UQ", CharacterType.HardCharacter, "OQ"),
        new TranscriptionEntry("𐱂", "aS", CharacterType.HardConsonant),
        new TranscriptionEntry("𐰽", "aS", CharacterType.HardConsonant),
        new TranscriptionEntry("𐰑", "aD", CharacterType.HardConsonant),
        new TranscriptionEntry("𐰒", "aD", CharacterType.HardConsonant),
        new TranscriptionEntry("𐰦", "NT", CharacterType.Character),
        new TranscriptionEntry("𐰍", "aĞ", CharacterType.HardConsonant),
        new TranscriptionEntry("𐰎", "aĞ", CharacterType.HardConsonant),
        new TranscriptionEntry("𐱈", "RT", CharacterType.Character),
        new TranscriptionEntry("𐰴", "aQ", CharacterType.HardConsonant),
        new TranscriptionEntry("𐰞", "aL", CharacterType.HardConsonant),
        new TranscriptionEntry("𐰟", "aL", CharacterType.HardConsonant),
        
        // Second Row - Shift
        new TranscriptionEntry("𐰾", "eS", CharacterType.SoftConsonant),
        new TranscriptionEntry("𐰓", "eD", CharacterType.SoftConsonant),
        new TranscriptionEntry("𐰨", "NÇ", CharacterType.Character),
        new TranscriptionEntry("𐰩", "NÇ", CharacterType.Character),
        new TranscriptionEntry("𐰏", "eG", CharacterType.SoftConsonant),
        new TranscriptionEntry("𐰐", "eG", CharacterType.SoftConsonant),
        new TranscriptionEntry("𐰡", "LT", CharacterType.Character),
        new TranscriptionEntry("𐰚", "eQ", CharacterType.SoftConsonant),
        new TranscriptionEntry("𐰛", "eQ", CharacterType.SoftConsonant),
        new TranscriptionEntry("𐰠", "eL", CharacterType.SoftConsonant),
        
        // Third Row
        new TranscriptionEntry("𐰕", "Z", CharacterType.HardConsonant_Single),
        new TranscriptionEntry("𐱀", "Ş", CharacterType.HardConsonant_Single),
        new TranscriptionEntry("𐰿", "Ş", CharacterType.HardConsonant_Single),
        new TranscriptionEntry("𐱁", "Ş", CharacterType.HardConsonant_Single),
        new TranscriptionEntry("𐰲", "Ç", CharacterType.HardConsonant_Single),
        new TranscriptionEntry("𐰳", "Ç", CharacterType.HardConsonant_Single),
        new TranscriptionEntry("𐰜", "ÜK", CharacterType.SoftCharacter, "ÖK"),
        new TranscriptionEntry("𐰰", "ÜK", CharacterType.SoftCharacter, "ÖK"),
        new TranscriptionEntry("𐰝", "ÖK", CharacterType.SoftCharacter, "ÜK"),
        new TranscriptionEntry("𐰉", "aB", CharacterType.HardConsonant),
        new TranscriptionEntry("𐰊", "aB", CharacterType.HardConsonant),
        new TranscriptionEntry("𐰣", "aN", CharacterType.HardConsonant),
        new TranscriptionEntry("𐰢", "M", CharacterType.HardConsonant_Single),
        
        // Third Row - Shift
        new TranscriptionEntry("𐰌", "eB", CharacterType.SoftConsonant),
        new TranscriptionEntry("𐰋", "eB", CharacterType.SoftConsonant),
        new TranscriptionEntry("𐰤", "eN", CharacterType.SoftConsonant),
        new TranscriptionEntry("𐰥", "eN", CharacterType.SoftConsonant),
        new TranscriptionEntry("𐰫", "NY", CharacterType.Character, "NY"),
        new TranscriptionEntry("𐰪", "NY", CharacterType.Character, "NY"),
    ];
    this.singleTranscriptionData = new SingleTranscriptionData();
}

CorrentText_Old.prototype.Initialize = function() {
    if (this._isInitialized) return;
    this._isInitialized = true;
    var o   = this._transcriptionList.find(x => x.transcription === "U");
    var oe  = this._transcriptionList.find(x => x.transcription === "Ö");
    var qi  = this._transcriptionList.find(x => x.transcription === "QI");
    var iq  = this._transcriptionList.find(x => x.transcription === "IQ");
    var oq  = this._transcriptionList.find(x => x.transcription === "OQ");
    var uq  = this._transcriptionList.find(x => x.transcription === "UQ");
    var oeq = this._transcriptionList.find(x => x.transcription === "ÖK");
    var ueq = this._transcriptionList.find(x => x.transcription === "ÜK");
    // no linking here per C#
};

CorrentText_Old.prototype.GetTranscription = function(inputText) {
    return this._GetTranscription(inputText, false);
};
CorrentText_Old.prototype.GetTranscription_Alternative = function(inputText) {
    return this._GetTranscription(inputText, true);
};

CorrentText_Old.prototype._GetTranscription = function(text, isAlternative) {
    this.Initialize();
    this.singleTranscriptionData = new SingleTranscriptionData();
    this.singleTranscriptionData.onFistVowelSet = function(x) { x.final = GetTranscriptionEntry(x); };
    this.singleTranscriptionData.onFistConsonantSet = function(x) {
        x.final = (x.type === CharacterType.SoftConsonant)
        ? x.transcriptionAlternative
        : x.transcription;
    };
    
    var textTemp = "";
    var index = 0;
    var lastArgument = this._transcriptionList[0];
    
    while (index < text.length) {
        if (isSurrogate(text, index) && !isSurrogatePair(text, index)) {
            index++;
            continue;
        }
        var codePoint = text.codePointAt(index);
        var symbol = String.fromCodePoint(codePoint);
        var transcriptionEntry = this._transcriptionList.find(t => t.symbol === symbol);
        if (!transcriptionEntry) {
            textTemp += symbol;
            index += symbol.length;
            continue;
        }
        
        var currentArgument = new TranscriptionEntry_Copy(transcriptionEntry);
        currentArgument.isAlternative = isAlternative;
        this.singleTranscriptionData.allProcessed.push(currentArgument);
        
        if (getUnicodeCategory(symbol) !== UnicodeCategory.Format) {
            switch (transcriptionEntry.type) {
                case CharacterType.Character:
                    ProcessCharacter.call(this, currentArgument, lastArgument);
                    break;
                case CharacterType.HardCharacter:
                    ProcessHardCharacter.call(this, currentArgument, lastArgument);
                    break;
                case CharacterType.SoftCharacter:
                    ProcessSoftCharacter.call(this, currentArgument, lastArgument);
                    break;
                case CharacterType.HardConsonant:
                    ProcessHardConsonant.call(this, currentArgument, lastArgument);
                    break;
                case CharacterType.HardConsonant_Single:
                    ProcessHardConsonant_Single.call(this, currentArgument, lastArgument);
                    break;
                case CharacterType.SoftConsonant:
                    ProcessSoftConsonant.call(this, currentArgument, lastArgument);
                    break;
                case CharacterType.HardVowel:
                    ProcessHardVowel.call(this, currentArgument, lastArgument);
                    break;
                case CharacterType.HardVowel_HasSoftVariant:
                    ProcessHardVowel_HasSoftVariant.call(this, currentArgument, lastArgument);
                    break;
                case CharacterType.SoftVowel:
                    ProcessSoftVowel.call(this, currentArgument, lastArgument);
                    break;
            }
        }
        
        index += symbol.length;
        lastArgument = transcriptionEntry;
    }
    
    var fv = this.singleTranscriptionData.firstVowel;
    if (fv && fv.recheck) fv.recheck();
    
    this.singleTranscriptionData.allProcessed.forEach(e => {
        textTemp += e.final;
    });
    
    return textTemp;
};
