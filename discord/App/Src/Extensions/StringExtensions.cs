using Cogmaster.Src.Data.Classes;
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

    public static string ConvertToIcon(this string input)
    {
        return (input) switch
        {
            "ARMOR" => Emotes.IconArmor,
            "DEPOT/SPRITE EGG" => Emotes.IconSprite,
            "BOMB" => Emotes.IconBomb,
            "HANDGUN" => Emotes.IconHandGun,
            "HELMET" => Emotes.IconHelmet,
            "HELM" => Emotes.IconHelmet,
            "SHIELD" => Emotes.IconShield,
            "SWORD" => Emotes.IconSword,
            "TRINKET" => Emotes.IconTrinket,
            _ => input
        };
    }

    public static string ConvertIfAccessorySlot(this string value)
    {
        return value switch
        {
            "HELM_TOP" => Emotes.HelmTop,
            "HELM_FRONT" => Emotes.HelmFront,
            "HELM_BACK" => Emotes.HelmBack,
            "HELM_SIDE" => Emotes.HelmSide,
            "ARMOR_FRONT" => Emotes.ArmorFront,
            "ARMOR_BACK" => Emotes.ArmorBack,
            "ARMOR_ANKLE" => Emotes.ArmorAnkle,
            "ARMOR_REAR" => Emotes.ArmorRear,
            "ARMOR_AURA" => Emotes.ArmorAura,
            _ => value
        };
    }

    public static string ConvertRarity(this string rarity)
    {
        if (!int.TryParse(rarity, out int parsed) || parsed < 0) return rarity;

        var sb = new StringBuilder();

        for (int i = 0; i < 5; i++)
        {
            sb.Append(i < parsed ? Emotes.StarFull : Emotes.StarEmpty);
        }

        return sb.ToString();
    }
}
