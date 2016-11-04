
Lemmatizer model taken from scroll/scroll-lang-en-bundle at commit afecbe30 on Nov 3rd 2016

___________________________________________________________________________________________


The lemmatizer model contains the list of inflected and the respective uninflected prefixes 
for the words in a specific language.

The format of the file is plain text where each line represents an inflected or uninflected 
list of suffixes for a given part of speech.

The format of a line is as follows:

POS_INFLECTED=value1, value2

or

POS_UNINFLECTED=value1, value2

Where POS is the name of the PartOfSpeech (currently supports only NOUN, VERB, ADVERB, ADJECTIVE).

Notice that for the file to be valid an uninflected line as well as an inflected line must be present 
for each of the POS define. Also the number of suffixes defined for the uninflected forms must match 
the number of suffixes defined for the relative inflected forms.