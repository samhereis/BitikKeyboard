// Helper functions (ES5 compatible)
function isNullOrWhitespace(input) {
    return !input || input.trim() === "";
}

function isSurrogate(text, index) {
    var code = text.charCodeAt(index);
    return code >= 0xD800 && code <= 0xDFFF;
}

function isSurrogatePair(text, index) {
    if (index + 1 >= text.length) return false;
    var high = text.charCodeAt(index);
    var low = text.charCodeAt(index + 1);
    return high >= 0xD800 && high <= 0xDBFF && low >= 0xDC00 && low <= 0xDFFF;
}

function getUnicodeCategory(ch) {
    // This is a placeholder. In the C# version, CharUnicodeInfo.GetUnicodeCategory is used.
    // Here, we simply return "Other" unless the char is a format control.
    return "Other";
}

var UnicodeCategory = {
    Format: "Format"
};

// TranscriptionEntry constructor (mimicking the C# class)
function TranscriptionEntry(symbol, latinTranscription, cyrillicTranscription, type, vowel, vowelPosition, forceAddPreviousVowel) {
    this.symbol = symbol;
    this.latinTranscription = latinTranscription;
    this.cyrillicTranscription = cyrillicTranscription;
    this.type = type;
    this.vowel = (typeof vowel !== "undefined" ? vowel : null);
    this.vowelPosition = (typeof vowelPosition !== "undefined" ? vowelPosition : 0);
    this.forceAddPreviousVowel = (typeof forceAddPreviousVowel !== "undefined" ? forceAddPreviousVowel : false);
}

// TranscriptionArgument constructor
function TranscriptionArgument() {
    this.transcriptionEntry = null;
    this.vowelBefore = "";
    this.vowel = "";
    this.final = "";
    this.arguments = [];
}

TranscriptionArgument.prototype.getDisplay = function() {
    return (!isNullOrWhitespace(this.final)) ? this.final : "null";
};

// CorrentText "class"
function CorrentText() {
    // Build the list identical to C# version
    this.transcriptionList = [
        new TranscriptionEntry("", "", "", -1, ""),

        // First Row
        new TranscriptionEntry("𐰶", "QI", "ҚЫ", 0, "I", 1),
        new TranscriptionEntry("𐰬", "aÑ", "аҢ", 1),
        new TranscriptionEntry("𐰧", "aÑ", "аҢ", 1),
        new TranscriptionEntry("𐰁", "A", "А", 4),
        new TranscriptionEntry("𐰀", "A", "А", 4),
        new TranscriptionEntry("𐰺", "aR", "аР", 1),
        new TranscriptionEntry("𐱄", "aT", "аТ", 1),
        new TranscriptionEntry("𐱃", "aT", "аТ", 1),
        new TranscriptionEntry("𐱇", "OT", "ОТ", 0, "O"),
        new TranscriptionEntry("𐰖", "aY", "аЙ", 1),
        new TranscriptionEntry("𐰗", "AY", "АЙ", 0, "A", 1),
        new TranscriptionEntry("𐰩", "U", "У", 4),
        new TranscriptionEntry("𐰃", "I", "Ы", 4),
        new TranscriptionEntry("𐰆", "O", "О", 4),
        new TranscriptionEntry("𐰯", "P", "П", 3),
        new TranscriptionEntry("𐰊", "F", "Ф", 3),

        // First Row - Shift
        new TranscriptionEntry("𐰷", "IQ", "ЫҚ", 0, "I"),
        new TranscriptionEntry("𐰭", "eÑ", "еҢ", 2),
        new TranscriptionEntry("𐰮", "eÑ", "еҢ", 2),
        new TranscriptionEntry("𐰅", "E", "Е", 5),
        new TranscriptionEntry("𐰂", "E", "Е", 5),
        new TranscriptionEntry("𐰼", "eR", "еР", 2),
        new TranscriptionEntry("𐱅", "eT", "еТ", 2),
        new TranscriptionEntry("𐰘", "eY", "еЙ", 2),
        new TranscriptionEntry("𐰇", "Ü", "Ү", 5),
        new TranscriptionEntry("𐰄", "İ", "И", 5),
        new TranscriptionEntry("𐰈", "Ö", "Ө", 5),

        // Second Row
        new TranscriptionEntry("𐰹", "OQ", "ОҚ", 0, "O"),
        new TranscriptionEntry("𐱂", "aS", "аС", 1),
        new TranscriptionEntry("𐰽", "SU", "СУ", 0, "U"),
        new TranscriptionEntry("𐰑", "aD", "аД", 1),
        new TranscriptionEntry("𐰒", "aD", "аД", 1),
        new TranscriptionEntry("𐰦", "NT", "НТ", 0),
        new TranscriptionEntry("𐰍", "aǦ", "аҒ", 1),
        new TranscriptionEntry("𐰎", "aǦ", "аҒ", 1),
        new TranscriptionEntry("𐱈", "RT", "РТ", 0),
        new TranscriptionEntry("𐰳", "aJ", "аЖ", 1),
        new TranscriptionEntry("𐰴", "aQ", "аҚ", 1),
        new TranscriptionEntry("𐰛", "H", "Х", 3),
        new TranscriptionEntry("𐰞", "aL", "аЛ", 1),
        new TranscriptionEntry("𐰟", "aL", "аЛ", 1),

        // Second Row - Shift
        new TranscriptionEntry("𐰸", "UQ", "УҚ", 0, "U"),
        new TranscriptionEntry("𐰾", "eS", "еС", 2),
        new TranscriptionEntry("𐰓", "eD", "еД", 2),
        new TranscriptionEntry("𐰨", "NÇ", "НЧ", 0),
        new TranscriptionEntry("𐰏", "eG", "еГ", 2),
        new TranscriptionEntry("𐰐", "eG", "еГ", 2),
        new TranscriptionEntry("𐰡", "LT", "ЛТ", 0),
        new TranscriptionEntry("𐰙", "eJ", "еЖ", 2),
        new TranscriptionEntry("𐰚", "eK", "еК", 2),
        new TranscriptionEntry("𐰠", "eL", "еЛ", 2),

        // Third Row
        new TranscriptionEntry("𐰕", "Z", "З", 3),
        new TranscriptionEntry("𐱀", "aŞ", "аЩ", 1),
        new TranscriptionEntry("𐰲", "Ç", "Ч", 3),
        new TranscriptionEntry("𐰱", "ÇƗ", "ЧЫ", 0, "Ɨ"),
        new TranscriptionEntry("𐰜", "ÜK", "ҮК", 0),
        new TranscriptionEntry("𐰉", "aB", "аБ", 1),
        new TranscriptionEntry("𐰋", "V", "В", 3),
        new TranscriptionEntry("𐰣", "aN", "аН", 1),
        new TranscriptionEntry("𐰢", "M", "М", 3),

        // Third Row - Shift
        new TranscriptionEntry("𐰿", "eŞ", "еШ", 2),
        new TranscriptionEntry("𐰝", "ÖK", "ӨК", 0),
        new TranscriptionEntry("𐰌", "eB", "еБ", 2),
        new TranscriptionEntry("𐰤", "eN", "еН", 2),
        new TranscriptionEntry("𐰥", "eN", "еН", 2)
    ];

    // This will hold our argument objects
    this.arguments = [];
}

CorrentText.prototype.GetTranscription = function(text) {
    var textTemp = "";
    // Clear previous arguments
    this.arguments = [];

    // Add initial default argument.
    var newArgument = new TranscriptionArgument();
    newArgument.vowel = "";
    newArgument.transcriptionEntry = this.transcriptionList[0];
    this.arguments.push(newArgument);

    var index = 0;
    while (index < text.length) {
        // Handle surrogate pairs as in C#
        if (isSurrogate(text, index)) {
            if (index + 1 >= text.length || !isSurrogatePair(text, index)) {
                index++;
                continue;
            }
        }
        // Get code point and symbol
        var codePoint = text.codePointAt ? text.codePointAt(index) : text.charCodeAt(index);
        var symbol = (String.fromCodePoint) ? String.fromCodePoint(codePoint) : String.fromCharCode(codePoint);

        // Find matching transcription data
        var transcriptionData = null;
        for (var i = 0; i < this.transcriptionList.length; i++) {
            if (this.transcriptionList[i].symbol === symbol) {
                transcriptionData = this.transcriptionList[i];
                break;
            }
        }
        if (transcriptionData === null) {
            index++;
            newArgument = new TranscriptionArgument();
            newArgument.vowel = "";
            newArgument.transcriptionEntry = this.transcriptionList[0];
            this.arguments.push(newArgument);
            textTemp += symbol;
            continue;
        }

        // Create a new argument using last argument's vowel values.
        newArgument = new TranscriptionArgument();
        newArgument.transcriptionEntry = this.transcriptionList[0];
        var lastTranscriptionData = this.arguments[this.arguments.length - 1];
        newArgument.vowelBefore = lastTranscriptionData.vowel;
        newArgument.vowel = lastTranscriptionData.vowel;
        this.arguments.push(newArgument);

        if (getUnicodeCategory(symbol.charAt(0)) !== UnicodeCategory.Format) {
            newArgument.transcriptionEntry = transcriptionData;
            // Process according to type
            switch (transcriptionData.type) {
                case 0:
                    // Strong Character
                    this.ProcessStrongCharacter(newArgument, lastTranscriptionData);
                    break;
                case 1:
                case 2:
                case 3:
                    // Hard, Soft, or Strong Consonant are handled by ProcessHardConsonant
                    this.ProcessHardConsonant(newArgument, lastTranscriptionData);
                    break;
                case 4:
                case 5:
                    // For Strong or Flexible Vowel, simply assign the latinTranscription
                    newArgument.final = transcriptionData.latinTranscription;
                    newArgument.vowel = transcriptionData.latinTranscription;
                    newArgument.vowelBefore = lastTranscriptionData.vowel;
                    break;
            }
        }
        index++;
        textTemp += newArgument.final;
    }
    return textTemp;
};

CorrentText.prototype.ProcessStrongCharacter = function(transcriptionArgument, lastTranscriptionArgumen) {
    // If the vowel field is null or whitespace, assign it; otherwise, update both.
    if (isNullOrWhitespace(transcriptionArgument.transcriptionEntry.vowel)) {
        transcriptionArgument.vowel = transcriptionArgument.transcriptionEntry.vowel;
    } else {
        transcriptionArgument.vowel = transcriptionArgument.transcriptionEntry.vowel;
        transcriptionArgument.vowelBefore = transcriptionArgument.transcriptionEntry.vowel;
    }
    transcriptionArgument.final = transcriptionArgument.transcriptionEntry.latinTranscription;
    return transcriptionArgument.final;
};

CorrentText.prototype.ProcessHardConsonant = function(transcriptionArgument, lastTranscriptionArgumen) {
    var isStrongConsonant = transcriptionArgument.transcriptionEntry.type === 3;
    var vowelSelf = (transcriptionArgument.transcriptionEntry.type === 2) ? "e" : "a";
    if (isStrongConsonant && !isNullOrWhitespace(lastTranscriptionArgumen.vowel)) {
        vowelSelf = lastTranscriptionArgumen.vowel.toLowerCase();
    }
    var replacedWithEmpty_Self = transcriptionArgument.transcriptionEntry.latinTranscription.replace(vowelSelf, "");

    // Branch if the previous transcriptionEntry type is -1.
    if (lastTranscriptionArgumen.transcriptionEntry.type === -1) {
        replacedWithEmpty_Self = transcriptionArgument.transcriptionEntry.latinTranscription.replace(vowelSelf, "");
        transcriptionArgument.final = replacedWithEmpty_Self;
        transcriptionArgument.vowel = vowelSelf;
        transcriptionArgument.arguments.push("Made first letter");
    } else {
        if (lastTranscriptionArgumen.transcriptionEntry.type === 2 || lastTranscriptionArgumen.transcriptionEntry.type === 3) {
            replacedWithEmpty_Self = transcriptionArgument.transcriptionEntry.latinTranscription.replace(vowelSelf, "");
        }
        var replacedWithLowerCase_LastVowel = transcriptionArgument.transcriptionEntry.latinTranscription
            .replace(vowelSelf, lastTranscriptionArgumen.vowel.toLowerCase());
        if (transcriptionArgument.transcriptionEntry.type === 3 && !isNullOrWhitespace(lastTranscriptionArgumen.vowel)) {
            replacedWithLowerCase_LastVowel = vowelSelf + transcriptionArgument.transcriptionEntry.latinTranscription;
        }
        // Switch on the type of the previous transcription entry
        switch (lastTranscriptionArgumen.transcriptionEntry.type) {
            case 0:
                if (!isNullOrWhitespace(lastTranscriptionArgumen.vowel)) {
                    transcriptionArgument.final = transcriptionArgument.transcriptionEntry.latinTranscription
                        .replace(vowelSelf, lastTranscriptionArgumen.vowel.toLowerCase());
                    transcriptionArgument.vowel = lastTranscriptionArgumen.vowel;
                    transcriptionArgument.arguments.push("Folowing last Strong Character");
                } else {
                    transcriptionArgument.final = replacedWithEmpty_Self;
                    transcriptionArgument.vowel = lastTranscriptionArgumen.vowel;
                    transcriptionArgument.vowelBefore = lastTranscriptionArgumen.vowel;
                    transcriptionArgument.arguments.push("Folowing last Strong Character with Vowel");
                }
                break;
            case 1:
                transcriptionArgument.final = replacedWithLowerCase_LastVowel;
                transcriptionArgument.vowel = lastTranscriptionArgumen.vowel;
                transcriptionArgument.arguments.push("Folowing last hard consonant");
                break;
            case 2:
                transcriptionArgument.final = replacedWithLowerCase_LastVowel;
                transcriptionArgument.vowel = lastTranscriptionArgumen.vowel;
                transcriptionArgument.arguments.push("Folowing last soft consonant");
                break;
            case 3:
                if (isNullOrWhitespace(lastTranscriptionArgumen.vowel)) {
                    transcriptionArgument.final = transcriptionArgument.transcriptionEntry.latinTranscription;
                } else {
                    // Set the previous argument's vowel to current vowelSelf
                    lastTranscriptionArgumen.vowel = vowelSelf;
                    transcriptionArgument.final = replacedWithLowerCase_LastVowel;
                }
                transcriptionArgument.vowel = lastTranscriptionArgumen.vowel;
                transcriptionArgument.arguments.push("Folowing last strong consonant");
                break;
            case 4:
                if (vowelSelf === lastTranscriptionArgumen.vowel.toLowerCase()) {
                    if (lastTranscriptionArgumen.vowel.toLowerCase() !== lastTranscriptionArgumen.vowelBefore.toLowerCase()) {
                        transcriptionArgument.final = replacedWithEmpty_Self;
                        transcriptionArgument.arguments.push("Same vowel found, so double vowel");
                    } else {
                        transcriptionArgument.final = lastTranscriptionArgumen.vowel + replacedWithEmpty_Self;
                        transcriptionArgument.arguments.push("Vowel change");
                    }
                } else {
                    transcriptionArgument.final = replacedWithEmpty_Self;
                    transcriptionArgument.arguments.push("Its vowel is different from standart vowel");
                }
                transcriptionArgument.vowel = lastTranscriptionArgumen.vowel;
                break;
            case 5:
                if (vowelSelf === lastTranscriptionArgumen.vowel.toLowerCase()) {
                    if (lastTranscriptionArgumen.vowel.toLowerCase() !== lastTranscriptionArgumen.vowelBefore.toLowerCase()) {
                        transcriptionArgument.final = replacedWithEmpty_Self;
                        transcriptionArgument.arguments.push("Same vowel found, so double vowel");
                    } else {
                        transcriptionArgument.final = lastTranscriptionArgumen.vowel + replacedWithEmpty_Self;
                        transcriptionArgument.arguments.push("Vowel change");
                    }
                } else {
                    transcriptionArgument.final = replacedWithEmpty_Self;
                    transcriptionArgument.arguments.push("Its vowel is different from standart vowel");
                }
                transcriptionArgument.vowel = lastTranscriptionArgumen.vowel;
                break;
        }
    }
    return transcriptionArgument.final;
};

CorrentText.prototype._Ref = function(transcriptionArgument, lastTranscriptionArgumen, isSoft) {
    isSoft = isSoft || false;
    // No processing is done in the C# version aside from the switch stub.
    return transcriptionArgument.final;
};
