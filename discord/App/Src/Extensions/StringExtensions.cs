using System.Text;

namespace Cogmaster.Src.Extensions;

public static class StringExtensions
{
    public static string ToTitleCase(this string input)
    {
        var words = input.Split(' ');
        var final = new StringBuilder();

        foreach (var word in words)
        {
            final.Append(string.Concat(word[0].ToString().ToUpperInvariant(), word.ToLowerInvariant().AsSpan(1)) + ' ');
        }

        return final.ToString().TrimEnd();
    }
}
