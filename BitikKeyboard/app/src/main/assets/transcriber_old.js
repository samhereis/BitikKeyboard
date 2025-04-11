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
    // Placeholder – in the C# version, char.GetUnicodeCategory is used.
    // Here we simply return "Other" (assuming non-format characters).
    return "Other";
}

var UnicodeCategory = {
    Format: "Format"
};

// TranscriptionEntry constructor (mirroring the C# class)
function TranscriptionEntry(symbol, transcription, type, vowel, vowelPosition) {
    this.symbol = symbol;
    this.transcription = transcription; // single transcription string
    this.type = type;
    this.vowel = (typeof vowel !== "undefined" ? vowel : null);
    this.vowelPosition = (typeof vowelPosition !== "undefined" ? vowelPosition : 0);
    // forceAddPreviousVowel is not used in this simplified logic.
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
    return !isNullOrWhitespace(this.final) ? this.final : "null";
};

// CorrentText "class"
function CorrentText() {
    // Build the transcription list exactly as in the C# code.
    this.transcriptionList = [
        new TranscriptionEntry("", "", -1, ""),
        // First Row
        new TranscriptionEntry("𐰶", "ҚЫ", 0, "Ы", 1),
        new TranscriptionEntry("𐰬", "аҢ", 1),
        new TranscriptionEntry("𐰧", "аҢ", 1),
        new TranscriptionEntry("𐰁", "А", 4),
        new TranscriptionEntry("𐰀", "А", 4),
        new TranscriptionEntry("𐰺", "аР", 1),
        new TranscriptionEntry("𐱄", "аТ", 1),
        new TranscriptionEntry("𐱃", "аТ", 1),
        new TranscriptionEntry("𐱇", "ОТ", 0, "O"),
        new TranscriptionEntry("𐰖", "аЙ", 1),
        new TranscriptionEntry("𐰗", "АЙ", 0, "А", 1),
        new TranscriptionEntry("𐰩", "У", 4),
        new TranscriptionEntry("𐰃", "Ы", 4),
        new TranscriptionEntry("𐰆", "О", 4),
        new TranscriptionEntry("𐰯", "П", 3),
        new TranscriptionEntry("𐰊", "Ф", 3),
        // First Row - Shift
        new TranscriptionEntry("𐰷", "ЫҚ", 0, "Ы"),
        new TranscriptionEntry("𐰭", "еҢ", 2),
        new TranscriptionEntry("𐰮", "еҢ", 2),
        new TranscriptionEntry("𐰅", "Е", 5),
        new TranscriptionEntry("𐰂", "Е", 5),
        new TranscriptionEntry("𐰼", "еР", 2),
        new TranscriptionEntry("𐱅", "еТ", 2),
        new TranscriptionEntry("𐰘", "еЙ", 2),
        new TranscriptionEntry("𐰇", "Ү", 5),
        new TranscriptionEntry("𐰄", "И", 5),
        new TranscriptionEntry("𐰈", "Ө", 5),
        // Second Row
        new TranscriptionEntry("𐰹", "ОҚ", 0, "О"),
        new TranscriptionEntry("𐱂", "аС", 1),
        new TranscriptionEntry("𐰽", "СУ", 0, "У"),
        new TranscriptionEntry("𐰑", "аД", 1),
        new TranscriptionEntry("𐰒", "аД", 1),
        new TranscriptionEntry("𐰦", "НТ", 0),
        new TranscriptionEntry("𐰍", "аҒ", 1),
        new TranscriptionEntry("𐰎", "аҒ", 1),
        new TranscriptionEntry("𐱈", "РТ", 0),
        new TranscriptionEntry("𐰳", "аЖ", 1),
        new TranscriptionEntry("𐰴", "аҚ", 1),
        new TranscriptionEntry("𐰛", "Х", 3),
        new TranscriptionEntry("𐰞", "аЛ", 1),
        new TranscriptionEntry("𐰟", "аЛ", 1),
        // Second Row - Shift
        new TranscriptionEntry("𐰸", "УҚ", 0, "У"),
        new TranscriptionEntry("𐰾", "еС", 2),
        new TranscriptionEntry("𐰓", "еД", 2),
        new TranscriptionEntry("𐰨", "НЧ", 0),
        new TranscriptionEntry("𐰏", "еГ", 2),
        new TranscriptionEntry("𐰐", "еГ", 2),
        new TranscriptionEntry("𐰡", "ЛТ", 0),
        new TranscriptionEntry("𐰙", "еЖ", 2),
        new TranscriptionEntry("𐰚", "еК", 2),
        new TranscriptionEntry("𐰠", "еЛ", 2),
        // Third Row
        new TranscriptionEntry("𐰕", "З", 3),
        new TranscriptionEntry("𐱀", "аЩ", 1),
        new TranscriptionEntry("𐰲", "Ч", 3),
        new TranscriptionEntry("𐰱", "ЧЫ", 0, "Ы"),
        new TranscriptionEntry("𐰜", "ҮК", 0),
        new TranscriptionEntry("𐰉", "аБ", 1),
        new TranscriptionEntry("𐰋", "В", 3),
        new TranscriptionEntry("𐰣", "аН", 1),
        new TranscriptionEntry("𐰢", "М", 3),
        // Third Row - Shift
        new TranscriptionEntry("𐰿", "еШ", 2),
        new TranscriptionEntry("𐰝", "ӨК", 0),
        new TranscriptionEntry("𐰌", "еБ", 2),
        new TranscriptionEntry("𐰤", "еН", 2),
        new TranscriptionEntry("𐰥", "еН", 2)
    ];

    // This will hold our transcription argument objects.
    this.arguments = [];
}

CorrentText.prototype.GetTranscription = function(text) {
    var textTemp = "";
    this.arguments = []; // Clear previous arguments

    // Add initial default argument.
    var currentArgument = new TranscriptionArgument();
    currentArgument.vowel = "";
    currentArgument.transcriptionEntry = this.transcriptionList[0];
    this.arguments.push(currentArgument);

    var index = 0;
    while (index < text.length) {
        // Handle surrogate pairs (as in the C# code)
        if (isSurrogate(text, index)) {
            if (index + 1 >= text.length || !isSurrogatePair(text, index)) {
                index++;
                continue;
            }
        }

        // Get the Unicode code point and corresponding symbol.
        var codePoint = (text.codePointAt) ? text.codePointAt(index) : text.charCodeAt(index);
        var symbol = (String.fromCodePoint) ? String.fromCodePoint(codePoint) : String.fromCharCode(codePoint);

        // Find matching transcription entry.
        var transcriptionData = null;
        for (var i = 0; i < this.transcriptionList.length; i++) {
            if (this.transcriptionList[i].symbol === symbol) {
                transcriptionData = this.transcriptionList[i];
                break;
            }
        }
        if (transcriptionData === null) {
            index++;
            currentArgument = new TranscriptionArgument();
            currentArgument.vowel = "";
            currentArgument.transcriptionEntry = this.transcriptionList[0];
            this.arguments.push(currentArgument);
            textTemp += symbol;
            continue;
        }

        // Create a new argument and carry over the vowel information from the last argument.
        currentArgument = new TranscriptionArgument();
        currentArgument.transcriptionEntry = this.transcriptionList[0];
        var lastTranscriptionData = this.arguments[this.arguments.length - 1];
        currentArgument.vowelBefore = lastTranscriptionData.vowel;
        currentArgument.vowel = lastTranscriptionData.vowel;
        this.arguments.push(currentArgument);

        // If the Unicode category is not Format, process according to the entry type.
        if (getUnicodeCategory(symbol.charAt(0)) !== UnicodeCategory.Format) {
            currentArgument.transcriptionEntry = transcriptionData;
            switch (transcriptionData.type) {
                case 0: // Strong Character
                    ProcessStrongCharacter(currentArgument, lastTranscriptionData);
                    break;
                case 1:
                case 2:
                case 3: // Hard, Soft, or Strong Consonant
                    ProcessHardConsonant(currentArgument, lastTranscriptionData);
                    break;
                case 4:
                case 5: // Vowels: simply assign the transcription.
                    currentArgument.final = transcriptionData.transcription;
                    currentArgument.vowel = transcriptionData.transcription;
                    currentArgument.vowelBefore = lastTranscriptionData.vowel;
                    break;
            }
        }

        index++;
        textTemp += currentArgument.final;
    }

    return textTemp;
};

function ProcessStrongCharacter(transcriptionArgument, lastTranscriptionData) {
    // Simply assign the transcription string to final.
    transcriptionArgument.final = transcriptionArgument.transcriptionEntry.transcription;
    return transcriptionArgument.final;
}

function ProcessHardConsonant(transcriptionArgument, lastTranscriptionData) {
    // Assign the final value as the last character of the transcription string.
    var str = transcriptionArgument.transcriptionEntry.transcription;
    transcriptionArgument.final = str.charAt(str.length - 1);
    return transcriptionArgument.final;
}